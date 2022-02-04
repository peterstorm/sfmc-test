package domain.errors

final case class HttpError(
  message: String
) extends BaseError(message)
