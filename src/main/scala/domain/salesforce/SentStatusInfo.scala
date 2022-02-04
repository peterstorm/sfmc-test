package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class SentStatusInfo(
  messageKey: String,
  contactKey: String,
  to: Option[String],
  statusCode: Option[Int],
  statusMessage: Option[String]
)

object SentStatusInfo:

  given Decoder[SentStatusInfo] = deriveDecoder[SentStatusInfo]
