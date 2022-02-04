package services

import fs2.Stream
import org.typelevel.log4cats.Logger
import algebras.http.PostClient
import domain.mailMessage._
import domain.auth.AccessToken
import domain.salesforce._
import domain.errors.ParseError
import domain.Attributes
import cats.parse.Parser
import cats.syntax.all._
import io.circe.syntax._
import cats.MonadThrow
import cats.effect.Concurrent

trait SalesforceMailService[F[_]]:

  def fetchAndPushMails: Stream[F, Unit]

object SalesforceMailService:

  def make[F[_]: Logger: MonadThrow](
    postClient: PostClient[F],
    mailMesageService: MailMessageService[F],
    isTest: String
  ): SalesforceMailService[F] =

    new SalesforceMailService:

      import parser.MailParser

      def fetchAndPushMails: Stream[F, Unit] =
        Stream
          .eval(mailMesageService.getPendingMails)
          .flatMap(Stream.emits(_))
          .evalMap(mail => convertMailToMessageDTO(mail) match
            case Right(dto) => 
              Logger[F].info(s"mailDto: ${dto.asJson.toString}") >>
              postClient.postEmail(dto, mail.mailMessage.id)
                .attempt
                .flatMap(either => either match
                  case Right(resp) =>
                    Logger[F].info(s"salesforce response: ${resp.toString}") >>
                    mailMesageService.commitMailMessageSent(mail.mailMessage, resp.requestId)
                  case Left(e)  =>
                    Logger[F].error(e.getMessage) >>
                    mailMesageService.setMailMessageError(mail.mailMessage, e.getMessage)
                )
            case Left(err)  => 
              handleParseError(mail, err)
          )

      def handleParseError(mail: Mail, err: Parser.Error): F[Unit] =
        val input = mail.content
        val lm = cats.parse.LocationMap(input)
        val pos = err.failedAtOffset
        val eofStr = "EOF"
        val error = s"error at: ${lm.toLineCol(pos)}, char: ${if (pos < input.length) input(pos) else eofStr}"
        Logger[F].error(error) >>
        mailMesageService.setMailMessageError(mail.mailMessage, error)

      def convertMailToMessageDTO(mail: Mail): Either[Parser.Error, MessageDTO] =
        val mailMessage = mail.mailMessage
        val mailMessageId = mailMessage.id
        val templateKey = mailMessage.templateKey
        val userId = mailMessage.objectId
        val email = mailMessage.toAddresses
        val content = MailParser.parseFile.parse(mail.content)
        val contactKey = if isTest.equals("TRUE") then "FOO_TEST" else userId
        content.map(v =>
          val attributes = v._2 match
            case Attributes.AMap(m) => 
              Attributes.AMap(m + ("MESSAGE_ID" -> Attributes.AString(mailMessageId)))
            case Attributes.AList(l) =>
              Attributes.AList(l)
            case Attributes.AString(s) =>
              Attributes.AString(s)
          MessageDTO(templateKey, Recipient(contactKey, email, Some(attributes)))
        )
