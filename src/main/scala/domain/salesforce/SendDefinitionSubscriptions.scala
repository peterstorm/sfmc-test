package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class SendDefinitionSubscriptions(
  shortCode: String,
  keyword: String,
  countryCode: Option[String],
  autoAddSubscriber: Option[Boolean],
  updateSubscriber: Option[Boolean]
)

object SendDefinitionSubscriptions:

  given Encoder[SendDefinitionSubscriptions] = deriveEncoder[SendDefinitionSubscriptions].mapJson(_.dropNullValues)
  given Decoder[SendDefinitionSubscriptions] = deriveDecoder[SendDefinitionSubscriptions]
