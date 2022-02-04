package algebras.repos

import domain.smsMessage.SmsMessage

trait SmsMessageRepo[F[_]]:

  def getSmsMessages: F[List[SmsMessage]]

  def commitSmsMessagesSent(sms: SmsMessage, sfRequestId: String): F[Unit]

  def setSmsMessageError(sms: SmsMessage, error: String): F[Unit]

  def setRetryCountAndStatus(sms: SmsMessage, error: String, retryCount: Int): F[Unit]

  def getSmsMessagesInError: F[List[SmsMessage]]

  def setSmsMessagePending(sms: SmsMessage): F[Unit]
