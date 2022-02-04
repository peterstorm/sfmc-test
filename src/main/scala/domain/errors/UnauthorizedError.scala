package domain.errors

final case class UnauthorizedError(
  message: String
) extends BaseError(message)
