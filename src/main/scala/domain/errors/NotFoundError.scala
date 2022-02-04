package domain.errors

final case class NotFoundError(
  message: String
) extends BaseError(message)
