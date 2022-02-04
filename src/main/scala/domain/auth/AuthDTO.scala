package domain.auth

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class AuthDTO(
  grant_type: String,
  client_id: String,
  client_secret: String,
  scope: String
)

object AuthDTO:

  given Decoder[AuthDTO] = deriveDecoder[AuthDTO]
  given Encoder[AuthDTO] = deriveEncoder[AuthDTO]
