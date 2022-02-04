package services

import cats.Monad
import cats.syntax.all._
import cats.conversions.all._
import fs2.Stream
import org.typelevel.log4cats.Logger
import algebras.repos.SmsMessageRepo
import algebras.http.GetClient
import domain.salesforce.SentStatus
import domain.mailMessage.MailMessage
import domain.smsMessage.SmsMessage

trait SalesforceErrorHandlerService[F[_]]:

  def run: Stream[F, Unit]

  def handleMailsInError: F[Unit]

  def handleSmsInError: F[Unit]

object SalesforceErrorHandlerService:

  def make[F[_]: Logger: Monad](
    smsRepo: SmsMessageRepo[F],
    mailService: MailMessageService[F],
    getClient: GetClient[F]
  ): SalesforceErrorHandlerService[F] =

    new SalesforceErrorHandlerService:

      def run: Stream[F, Unit] =
        Stream.eval(handleMailsInError) ++ Stream.eval(handleSmsInError)

      def handleMailsInError: F[Unit] =
        getEmailSentStatus
          .flatMap( list =>
            list.traverse { case (ss, mm) =>
              ss match
                case dto: SentStatus.SentStatusDTO =>
                  dto.eventCategoryType match
                    case "TransactionalSendEvents.EmailSent" =>
                      mailService.commitMailMessageSent(mm, dto.requestId)
                    case "TransactionalSendEvents.EmailNotSent" =>
                      mailService.setMailMessageError(mm, dto.info.get.statusMessage.get)
                    case "TransactionalSendEvents.EmailQueued" =>
                      mailService.setMailMessageError(mm, "EmailQueued, do manual check")
                case error: SentStatus.SentStatusError =>
                  mailService.setMailMessagePending(mm)
            }
          ).void

      def handleSmsInError: F[Unit] =
        getSmsSentStatus
          .flatMap( list =>
            list.traverse { case (ss, sms) =>
              ss match
                case dto: SentStatus.SentStatusDTO =>
                  dto.eventCategoryType match
                    case "TransactionalSendEvents.SMSSent" =>
                      smsRepo.commitSmsMessagesSent(sms, dto.requestId)
                    case "TransactionalSendEvents.SMSNotSent" =>
                      smsRepo.setSmsMessageError(sms, dto.info.get.statusMessage.get)
                    case "TransactionalSendEvents.SMSQueued" =>
                      smsRepo.setSmsMessageError(sms, "SMSQueued, do manual check")
                case error: SentStatus.SentStatusError =>
                  smsRepo.setSmsMessagePending(sms)
            }
          ).void

      def getEmailSentStatus: F[List[(SentStatus, MailMessage)]] =
        Logger[F].info("getEmailStatus ran") >>
        mailService
          .getMailMessagesInError
          .flatMap( list =>
            list.traverse(mm => getClient.getEmailStatus(mm.id).tupleRight(mm))
          )

      def getSmsSentStatus: F[List[(SentStatus, SmsMessage)]] =
        Logger[F].info("getSmsSentStatus ran") >>
        smsRepo
          .getSmsMessagesInError
          .flatMap( list =>
            list.traverse(sms => getClient.getSmsStatus(sms.id).tupleRight(sms))
          )
