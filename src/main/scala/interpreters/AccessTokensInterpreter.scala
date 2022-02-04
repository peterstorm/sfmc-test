package interpreters

import cats.MonadThrow
import cats.syntax.all._
import algebras.AccessTokens
import services.AuthService
import domain.auth.AccessToken
import domain.errors.{ForbiddenError, UnauthorizedError, RetryError}
import org.typelevel.log4cats.Logger

class AccessTokensInterpreter[F[_]: MonadThrow: Logger](
  authService: AuthService[F]
) extends AccessTokens[F]:

  def withAccessToken[A](f: AccessToken => F[A]): F[A] =
    authService.retrieveAccessToken
      .flatMap( token => f(token))
      .handleErrorWith( e => e match
        case ua: UnauthorizedError =>
          authService.renewAccessToken >>= (token => retry(f(token), 0, ua.message))
        case fb: ForbiddenError =>
          authService.renewAccessToken >>= (token => retry(f(token), 0, fb.message))
        case _ =>
          e.raiseError[F, A]
      )

  private def retry[A](action: F[A], retries: Int, originalError: String): F[A] =
    if retries > 3 then RetryError(s"too many retries: ${originalError}").raiseError[F, A]
    else
      Logger[F].info(s"retry called with $retries count and error: $originalError") >>
      action.attempt.flatMap( either => either match
        case Left(e) => e match
          case ua: UnauthorizedError => retry(action, retries + 1, e.getMessage)
          case fb: ForbiddenError => retry(action, retries + 1, e.getMessage)
          case _                  => e.raiseError[F, A]
        case Right(x) => x.pure[F]
      )

object AccessTokensInterpreter:

  def apply[F[_]: MonadThrow: Logger](
    authService: AuthService[F]
  ): AccessTokensInterpreter[F] =
    new AccessTokensInterpreter(authService)
