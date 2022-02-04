package domain.salesforce

import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.circe.generic.auto._
import cats.syntax.functor._

enum SentStatus:

  case SentStatusDTO(
    requestId: String,
    eventCategoryType: String,
    timestamp: String,
    compositeId: Option[String],
    info: Option[SentStatusInfo]
  )

  case SentStatusError(
    message: String,
    errorcode: Int,
    documentation: String
  )

object SentStatus:

  given Encoder[SentStatus] =
    Encoder.instance {
      case dto @ SentStatus.SentStatusDTO(_,_,_,_,_) => dto.asJson
      case error @ SentStatus.SentStatusError(_,_,_) => error.asJson
    }

  given Decoder[SentStatus] =
    List[Decoder[SentStatus]](
      Decoder[SentStatusDTO].widen,
      Decoder[SentStatusError].widen
    ).reduceLeft(_ or _)
