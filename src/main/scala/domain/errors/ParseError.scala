package domain.errors

final case class ParseError(
  message: String
) extends BaseError(message)
