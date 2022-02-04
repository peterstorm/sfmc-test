package domain.errors

final case class RetryError(
  message: String
) extends BaseError(message)
