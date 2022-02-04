package domain.salesforce

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

final case class MessageResponseDTO(
  requestId: String,
  errorcode: Int,
  responses: List[Responses]
)

object MessageResponseDTO:

  given Decoder[MessageResponseDTO] = deriveDecoder[MessageResponseDTO]
  given Encoder[MessageResponseDTO] = deriveEncoder[MessageResponseDTO]
