package interpreters.repos

import cats.effect.MonadCancel
import cats.syntax.all._
import capabilities.database.Database
import domain.mailMessage.{MailMessage, MailContent}
import algebras.repos.MailMessageRepo
import doobie.util.query.Query0
import doobie.syntax.all._
import doobie.implicits._
import doobie.util.update.Update0
import java.time.format.DateTimeFormatter

class MailMessageRepoInterpreter[F[_]](
  using F: MonadCancel[F, Throwable], DB: Database[F]
) extends MailMessageRepo[F]:

  import MailMessageSQL._
  val xa = Database[F].transactor

  def getMailMessages: F[List[MailMessage]] =
    getMailMessagesSQL.to[List].transact(xa)

  def getMailContent(mailMessage: MailMessage): F[List[MailContent]] =
    getMailContentSQL(mailMessage).to[List].transact(xa)

  def commitMailMessageSent(mailMessage: MailMessage, sfRequestId: String): F[Unit] =
    val action =
      for
        _ <- deleteMailMessageSQL(mailMessage).run
        _ <- insertMailMessageSentSQL(mailMessage, sfRequestId).run
      yield ()
    action.transact(xa)

  def setMailMessageError(mailMessage: MailMessage, error: String): F[Unit] =
    setMailMessageErrorSQL(mailMessage, error).run.transact(xa).void

  def getMailMessagesInError: F[List[MailMessage]] =
    getMailMessagesInErrorSQL.to[List].transact(xa)

  def setMailMessagePending(mailMessage: MailMessage): F[Unit] =
    setMailMessagePendingSQL(mailMessage).run.transact(xa).void

object MailMessageRepoInterpreter:

  def apply[F[_]](
    using F: MonadCancel[F, Throwable], DB: Database[F]
  ): MailMessageRepoInterpreter[F] =
    new MailMessageRepoInterpreter

object MailMessageSQL:

  val receivedDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")
  val date = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

  def getMailMessagesSQL: Query0[MailMessage] = sql"""
    SELECT 
      MM.ID, MM.STATUS_ID, MM.CREATE_DATE, MM.TO_ADDRESSES, MM.SUBJECT, MM.FROM_NAME, MM.FROM_EMAIL, MM.TYPE_ID,
      MM.ERROR_MESSAGE, MM.SEND_FROM_DATE, MM.FROM_USER, MT.MAIL_KEY, MM.MAILTEMPLATE_ID, MM.OBJECT_ID, MM.OBJECT_CLASS
    FROM 
      MAIL_MESSAGE MM, MAILTEMPLATE MT
    WHERE 
      MM.MAILTEMPLATE_ID IN (SELECT ID FROM MAILTEMPLATE WHERE MAIL_KEY LIKE 'SFMC%')
    AND MM.MAILTEMPLATE_ID = MT.ID
    AND MM.STATUS_ID IN (0, 70) 
    AND NVL(MM.SEND_FROM_DATE, SYSDATE) <= SYSDATE
	AND ROWNUM < 1000
    """.query[MailMessage]

  def getMailContentSQL(mailMessage: MailMessage): Query0[MailContent] = sql"""
    SELECT
      ID, MESSAGE_ID, SEQ, MESSAGE
    FROM 
      MAIL_CONTENT
    WHERE MESSAGE_ID = ${mailMessage.id}
    """.query[MailContent]

  def deleteMailMessageSQL(mailMessage: MailMessage): Update0 = sql"""
    DELETE
    FROM MAIL_MESSAGE
    WHERE
      ID = ${mailMessage.id}
    """.update

  def insertMailMessageSentSQL(mailMessage: MailMessage, sfRequestId: String): Update0 = sql"""
    INSERT INTO MAIL_MESSAGE_SENT
      (ID, STATUS_ID, CREATE_DATE, SENT_DATE, TO_ADDRESSES, SUBJECT, MESSAGE, FROM_NAME, FROM_EMAIL, TYPE_ID
      , FROM_USER, MAILTEMPLATE_ID, OBJECT_ID, OBJECT_CLASS
      )
    VALUES(${mailMessage.id}, 1
      , TO_DATE(${date.format(receivedDate.parse(mailMessage.createDate))}, 'DD/MM/YYYY HH24:MI:SS')
      , SYSDATE, ${mailMessage.toAddresses}, ${mailMessage.subject}, $sfRequestId, ${mailMessage.fromName}
      , ${mailMessage.fromEmail}, ${mailMessage.typeId.toInt}, ${mailMessage.fromUser}, ${mailMessage.templateId}
      , ${mailMessage.objectId}, ${mailMessage.objectClass}
      )
    """.update

  def setMailMessageErrorSQL(mailMessage: MailMessage, error: String): Update0 = sql"""
    UPDATE MAIL_MESSAGE
    SET
      STATUS_ID = 9, ERROR_MESSAGE = $error
    WHERE ID = ${mailMessage.id}
    """.update

  def getMailMessagesInErrorSQL: Query0[MailMessage] = sql"""
    SELECT 
      MM.ID, MM.STATUS_ID, MM.CREATE_DATE, MM.TO_ADDRESSES, MM.SUBJECT, MM.FROM_NAME, MM.FROM_EMAIL, MM.TYPE_ID,
      MM.ERROR_MESSAGE, MM.SEND_FROM_DATE, MM.FROM_USER, MT.MAIL_KEY, MM.MAILTEMPLATE_ID, MM.OBJECT_ID, MM.OBJECT_CLASS
    FROM 
      MAIL_MESSAGE MM, MAILTEMPLATE MT
    WHERE 
      MM.MAILTEMPLATE_ID IN (SELECT ID FROM MAILTEMPLATE WHERE MAIL_KEY LIKE 'SFMC%')
    AND MM.MAILTEMPLATE_ID = MT.ID
    AND MM.STATUS_ID = 9
    AND NVL(MM.SEND_FROM_DATE, SYSDATE) <= SYSDATE
    AND ROWNUM < 1000
    """.query[MailMessage]

  def setMailMessagePendingSQL(mailMessage: MailMessage): Update0 = sql"""
    UPDATE MAIL_MESSAGE
    SET
      STATUS_ID = 0
    WHERE ID = ${mailMessage.id}
    """.update


