package interpreters.repos

import cats.effect.MonadCancel
import cats.syntax.all._
import capabilities.database.Database
import domain.smsMessage.SmsMessage
import algebras.repos.SmsMessageRepo
import doobie.util.query.Query0
import doobie.syntax.all._
import doobie.implicits._
import doobie.util.update.Update0
import java.time.format.DateTimeFormatter

class SmsMessageRepoInterpreter[F[_]](
  using F: MonadCancel[F, Throwable], DB: Database[F]
) extends SmsMessageRepo[F]:

  import SmsMessageSQL._
  val xa = DB.transactor

  def getSmsMessages: F[List[SmsMessage]] =
    getSmsMessagesSQL.to[List].transact(xa)

  def commitSmsMessagesSent(sms: SmsMessage, sfRequestId: String): F[Unit] =
    commitSmsMessagesSentSQL(sms, sfRequestId).run.transact(xa).void

  def setSmsMessageError(sms: SmsMessage, error: String): F[Unit] =
    setSmsMessageErrorSQL(sms, error).run.transact(xa).void

  def setRetryCountAndStatus(sms: SmsMessage, error: String, retryCount: Int): F[Unit] =
    setRetryCountAndStatusSQL(sms, error, retryCount).run.transact(xa).void 

  def getSmsMessagesInError: F[List[SmsMessage]] =
    getSmsMessagesInErrorSQL.to[List].transact(xa)

  def setSmsMessagePending(sms: SmsMessage): F[Unit] =
    setSmsMessagePendingSQL(sms).run.transact(xa).void

object SmsMessageSQL:

  def getSmsMessagesSQL: Query0[SmsMessage] =
    sql"""
    SELECT
      SMS.ID, SMS.STATUS_ID, SMS.MOBILENUMBER, SMS.MESSAGE, MT.MAIL_KEY, SMS.DATECREATE,
      SMS.SEND_DATE, SMS.RESPONSE, SMS.MAILTEMPLATE_ID, SMS.RETRIES, SMS.OBJECT_ID, SMS.OBJECT_CLASS
    FROM
      SMS_MESSAGES SMS, MAILTEMPLATE MT
    WHERE
      SMS.MAILTEMPLATE_ID = MT.ID
    AND SMS.STATUS_ID = 50
    AND (SMS.SEND_DATE IS NULL OR SMS.SEND_DATE < SYSDATE)
    AND ROWNUM < 1000
    """.query[SmsMessage]

  def commitSmsMessagesSentSQL(sms: SmsMessage, sfRequestId: String): Update0 =
    sql"""
      UPDATE SMS_MESSAGES
      SET
        STATUS_ID = 51, SEND_DATE = SYSDATE, RESPONSE = $sfRequestId
      WHERE ID = ${sms.id}
    """.update

  def setSmsMessageErrorSQL(sms: SmsMessage, error: String): Update0 =
    sql"""
      UPDATE SMS_MESSAGES
      SET STATUS_ID = 53, RESPONSE = $error
      WHERE ID = ${sms.id}
    """.update

  def setRetryCountAndStatusSQL(sms: SmsMessage, error: String, retryCount: Int): Update0 =
    sql"""
      UPDATE SMS_MESSAGES
      SET STATUS_ID = 60, RESPONSE = $error, RETRIES = $retryCount
      WHERE ID = ${sms.id}
    """.update

  def getSmsMessagesInErrorSQL: Query0[SmsMessage] =
    sql"""
    SELECT
      SMS.ID, SMS.STATUS_ID, SMS.MOBILENUMBER, SMS.MESSAGE, MT.MAIL_KEY, SMS.DATECREATE,
      SMS.SEND_DATE, SMS.RESPONSE, SMS.MAILTEMPLATE_ID, SMS.RETRIES, SMS.OBJECT_ID, SMS.OBJECT_CLASS
    FROM
      SMS_MESSAGES SMS, MAILTEMPLATE MT
    WHERE
      SMS.MAILTEMPLATE_ID = MT.ID
    AND SMS.STATUS_ID = 53
    AND (SMS.SEND_DATE IS NULL OR SMS.SEND_DATE < SYSDATE)
    AND ROWNUM < 1000
    """.query[SmsMessage]

  def setSmsMessagePendingSQL(sms: SmsMessage): Update0 =
    sql"""
      UPDATE SMS_MESSAGES
      SET STATUS_ID = 50
      WHERE ID = ${sms.id}
    """.update
