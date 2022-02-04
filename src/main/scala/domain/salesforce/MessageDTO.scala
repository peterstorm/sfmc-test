package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class MessageDTO(
  definitionKey: String,
  recipient: Recipient,
)

object MessageDTO:

  given Encoder[MessageDTO] = deriveEncoder[MessageDTO]
