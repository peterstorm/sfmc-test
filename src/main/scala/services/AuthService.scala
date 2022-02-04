package services

import algebras.repos.AuthRepo
import domain.auth._
import cats.Monad
import cats.syntax.all._
import algebras.http.AuthClient

trait AuthService[F[_]]:

  def renewAccessToken: F[AccessToken]

  def retrieveAccessToken: F[AccessToken]

object AuthService:

  def make[F[_]: Monad](
    repo: AuthRepo[F],
    client: AuthClient[F]
  ): AuthService[F] =

    new AuthService[F]:

      def renewAccessToken: F[AccessToken] =
        repo
          .getClientCredentials
          .flatMap( creds => client.renewAccessToken(creds))
          .flatMap( token => repo.persistAccessToken(token) >> token.pure[F])

      def retrieveAccessToken: F[AccessToken] =
        repo.retrieveAccessToken
