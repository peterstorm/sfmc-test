package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class SendDefinitionResponseDTO(
  definitionKey: String,
  name: String,
  description: String,
  status: String,
  createdDate: String,
  modifiedDate: String,
  content: SendDefinitionContent,
  subscriptions: SendDefinitionSubscriptions
)

object SendDefinitionResponseDTO:

  given Encoder[SendDefinitionResponseDTO] = deriveEncoder[SendDefinitionResponseDTO]
  given Decoder[SendDefinitionResponseDTO] = deriveDecoder[SendDefinitionResponseDTO]
