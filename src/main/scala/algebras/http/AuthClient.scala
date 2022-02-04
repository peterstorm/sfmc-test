package algebras.http

import domain.auth._

trait AuthClient[F[_]]:

  def renewAccessToken(credentials: ClientCredentials): F[AccessToken]
