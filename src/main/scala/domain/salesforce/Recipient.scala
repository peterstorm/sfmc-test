package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import domain.Attributes

final case class Recipient(
  contactKey: String,
  to: String,
  attributes: Option[Attributes]
)

object Recipient:

  given Encoder[Recipient] = deriveEncoder[Recipient].mapJson(_.dropNullValues)
