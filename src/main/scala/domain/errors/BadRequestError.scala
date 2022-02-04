package domain.errors

final case class BadRequestError(
  message: String
) extends BaseError(message)
