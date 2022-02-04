package algebras.repos

import domain.auth.{ClientCredentials, AccessToken}

trait AuthRepo[F[_]]:

  def getClientCredentials: F[ClientCredentials]

  def getTestSettings: F[String]

  def persistAccessToken(token: AccessToken): F[Unit]

  def retrieveAccessToken: F[AccessToken]
