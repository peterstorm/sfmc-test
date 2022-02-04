package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class SmsMessageDTO(
  definitionKey: String,
  recipient: Recipient,
  content: SendDefinitionContent
)

object SmsMessageDTO:

  given Encoder[SmsMessageDTO] = deriveEncoder[SmsMessageDTO]


