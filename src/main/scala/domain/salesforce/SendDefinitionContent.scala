package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class SendDefinitionContent(
  message: String
)

object SendDefinitionMessage:

  given Encoder[SendDefinitionContent] = deriveEncoder[SendDefinitionContent]
  given Decoder[SendDefinitionContent] = deriveDecoder[SendDefinitionContent]
