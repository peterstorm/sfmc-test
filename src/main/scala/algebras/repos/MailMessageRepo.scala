package algebras.repos

import domain.mailMessage._

trait MailMessageRepo[F[_]]:

  def getMailMessages: F[List[MailMessage]]

  def getMailContent(mailMessage: MailMessage): F[List[MailContent]]

  def commitMailMessageSent(mailMessage: MailMessage, sfRequestId: String): F[Unit]

  def setMailMessageError(mailMessage: MailMessage, error: String): F[Unit]

  def getMailMessagesInError: F[List[MailMessage]]

  def setMailMessagePending(mailMessage: MailMessage): F[Unit]
