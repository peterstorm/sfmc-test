package domain.mailMessage

final case class MailContent(
  id: String,
  messageId: String,
  sequence: String,
  message: String
)
