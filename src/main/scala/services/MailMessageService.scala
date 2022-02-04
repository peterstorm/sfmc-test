package services

import org.typelevel.log4cats.Logger
import algebras.repos.MailMessageRepo
import domain.mailMessage._
import cats.syntax.all._
import cats.Monad

trait MailMessageService[F[_]]:

  def getPendingMails: F[List[Mail]]

  def commitMailMessageSent(mailMessage: MailMessage, sfRequestId: String): F[Unit]

  def setMailMessageError(mailMessage: MailMessage, error: String): F[Unit]

  def getMailMessagesInError: F[List[MailMessage]]

  def setMailMessagePending(mailMessage: MailMessage): F[Unit]

object MailMessageService:

  def make[F[_]: Logger: Monad](
    mailMessageRepo: MailMessageRepo[F]
  ): MailMessageService[F] =

    new MailMessageService[F]:

      def getPendingMails: F[List[Mail]] =
        mailMessageRepo.getMailMessages.flatMap(l =>
          l.traverse(mm =>
            mailMessageRepo.getMailContent(mm).map(mc =>
              Mail(mm, mc.foldLeft("")((b, a) => b |+| a.message))
            )
          )
        )

      def commitMailMessageSent(mailMessage: MailMessage, sfRequestId: String): F[Unit] =
        mailMessageRepo.commitMailMessageSent(mailMessage, sfRequestId)

      def setMailMessageError(mailMessage: MailMessage, error: String): F[Unit] =
        mailMessageRepo.setMailMessageError(mailMessage, error)

      def getMailMessagesInError: F[List[MailMessage]] =
        mailMessageRepo.getMailMessagesInError

      def setMailMessagePending(mailMessage: MailMessage): F[Unit] =
        mailMessageRepo.setMailMessagePending(mailMessage)
