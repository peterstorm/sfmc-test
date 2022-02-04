package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class SendDefinitionDTO(
  definitionKey: String,
  name: String,
  description: String,
  content: SendDefinitionContent,
  subscriptions: SendDefinitionSubscriptions
)

object SendDefinitionDTO:

  given Encoder[SendDefinitionDTO] = deriveEncoder[SendDefinitionDTO]
