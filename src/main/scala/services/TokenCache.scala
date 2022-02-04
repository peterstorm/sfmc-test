package services

import cats.effect.syntax.all._
import cats.effect.{Deferred, Ref, Temporal}
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import domain.auth.AccessToken

import scala.concurrent.duration._


trait TokenCache[F[_]]:

  def get: F[AccessToken]

  def expire: F[Unit]

object TokenCache:

  def make[F[_]: Temporal: Logger](
    refreshAction: F[AccessToken],
    refreshTime: FiniteDuration
  ): F[TokenCache[F]] =

    enum State:
      case Token(a: AccessToken)
      case Fetching(d: Deferred[F, Either[Throwable, AccessToken]])
      case Expired

    Ref.of[F, State](State.Expired).map( state =>

      new TokenCache[F]:

        def get: F[AccessToken] =
          Deferred[F, Either[Throwable, AccessToken]].flatMap( newValue =>
            state.modify{
              case st @ State.Token(a) => st -> a.pure[F]
              case st @ State.Fetching(value) => st -> value.get.rethrow
              case State.Expired => State.Fetching(newValue) -> refreshToken(newValue).rethrow
            }.flatten
          )

        def refreshToken(d: Deferred[F, Either[Throwable, AccessToken]]) =
          Temporal[F].uncancelable( poll =>
            for
              either <- poll(refreshAction.attempt).onCancel(
                          state.set(State.Expired) >> 
                          d.complete(new Exception("refreshToken failed").asLeft).void
                        )
              _ <- state.set(
                     either match
                       case Left(_) => State.Expired
                       case Right(v) => State.Token(v)
                   )
              _ <- d.complete(either)
            yield either
          )

        def expire: F[Unit] =
          def loop: F[Unit] =
            val effect = state.update{
              case State.Token(_) => State.Expired
              case State.Expired => State.Expired
              case st @ State.Fetching(_) => st
            }
            Temporal[F].sleep(refreshTime) >> effect >> Logger[F].info("expire loop ran") >> loop
          loop
    )

