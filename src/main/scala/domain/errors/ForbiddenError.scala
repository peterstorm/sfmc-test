package domain.errors

final case class ForbiddenError(
  message: String
) extends BaseError(message)
