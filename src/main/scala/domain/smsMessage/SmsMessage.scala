package domain.smsMessage

final case class SmsMessage(
  id: String,
  statusId: String,
  mobileNumber: String,
  message: String,
  templateKey: String,
  dateCreate: Option[String],
  sendDate: Option[String],
  response: Option[String],
  mailTemplateId: String,
  retries: Option[Int],
  objectId: String,
  objectClass: String
)
