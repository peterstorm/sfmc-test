package domain.auth

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class AccessToken(
  access_token: String
)

object AccessToken:

  given Decoder[AccessToken] = deriveDecoder[AccessToken]
  given Encoder[AccessToken] = deriveEncoder[AccessToken]
