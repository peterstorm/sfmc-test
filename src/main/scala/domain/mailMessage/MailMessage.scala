package domain.mailMessage

final case class MailMessage(
  id: String,
  statusId: String,
  createDate: String,
  toAddresses: String,
  subject: String,
  fromName: String,
  fromEmail: String,
  typeId: String,
  errorMessage: Option[String],
  sendFromDate: Option[String],
  fromUser: String,
  templateKey: String,
  templateId: String,
  objectId: String,
  objectClass: String
)
