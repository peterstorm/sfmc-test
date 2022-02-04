package algebras

import domain.auth.AccessToken

trait AccessTokens[F[_]]:

  def withAccessToken[A](f: AccessToken => F[A]): F[A]
