package domain.auth

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._


final case class ClientSecret(
  value: String
)

object ClientSecret:

  given Decoder[ClientSecret] = deriveDecoder[ClientSecret]
  given Encoder[ClientSecret] = deriveEncoder[ClientSecret]
