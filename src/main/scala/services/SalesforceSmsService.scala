package services

import fs2.Stream
import org.typelevel.log4cats.Logger
import algebras.http.PostClient
import algebras.repos.SmsMessageRepo
import domain.smsMessage.SmsMessage
import domain.auth.AccessToken
import domain.salesforce._
import domain.errors.ParseError
import domain.Attributes
import cats.parse.Parser
import cats.syntax.all._
import io.circe.syntax._
import cats.MonadThrow
import cats.effect.Concurrent

trait SalesforceSmsService[F[_]]:

  def fetchAndPushSms: Stream[F, Unit]

object SalesforceSmsService:

  def make[F[_]: Logger: MonadThrow](
    postClient: PostClient[F],
    repo: SmsMessageRepo[F],
    isTest: String
  ): SalesforceSmsService[F] =

    new SalesforceSmsService:

      def fetchAndPushSms: Stream[F, Unit] =
        Stream
          .eval(repo.getSmsMessages)
          .flatMap(Stream.emits(_))
          .evalMap(sms =>
            val dto = convertSmsToMessageDTO(sms)
            Logger[F].info(s"smsDTO: ${dto.asJson.toString}") >>
            postClient.postSms(dto, sms.id)
              .attempt
              .flatMap(either => either match
                case Right(resp) =>
                  Logger[F].info(s"salesforce response: ${resp.toString}") >>
                  repo.commitSmsMessagesSent(sms, resp.requestId)
                case Left(e)  =>
                  Logger[F].error(e.getMessage) >>
                  repo.setSmsMessageError(sms, e.getMessage)
              )
          )

      def handleRetry(sms: SmsMessage, error: String): F[Unit] =
        sms.retries match
          case None    => repo.setRetryCountAndStatus(sms, error, 0)
          case Some(i) =>
            if i >= 5
            then repo.setSmsMessageError(sms, error)
            else repo.setRetryCountAndStatus(sms, error, i + 1)

      def convertSmsToMessageDTO(sms: SmsMessage): SmsMessageDTO =
        val smsMessageId = sms.id
        val templateKey = sms.templateKey
        val userId = sms.objectId
        val number = "45" |+| sms.mobileNumber
        val content = sms.message
        val contactKey = if isTest.equals("TRUE") then "FOO_TEST" else userId
        SmsMessageDTO(
          templateKey,
          Recipient(contactKey, number, None),
          SendDefinitionContent(content)
        )
