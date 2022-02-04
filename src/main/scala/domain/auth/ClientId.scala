package domain.auth

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._


final case class ClientId(
  value: String
)

object ClientId:

  given Decoder[ClientId] = deriveDecoder[ClientId]
  given Encoder[ClientId] = deriveEncoder[ClientId]
