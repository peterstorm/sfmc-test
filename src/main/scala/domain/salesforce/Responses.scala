package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class Responses(
  messageKey: String
)

object Responses:

  given Decoder[Responses] = deriveDecoder[Responses]
  given Encoder[Responses] = deriveEncoder[Responses]
