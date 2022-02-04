package domain.auth

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class ClientCredentials(
  clientId: ClientId,
  clientSecret: ClientSecret
)

object ClientCredentials:

  given Decoder[ClientCredentials] = deriveDecoder[ClientCredentials]
  given Encoder[ClientCredentials] = deriveEncoder[ClientCredentials]
