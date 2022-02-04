package sfmc

import capabilities.configuration.Configuration
import capabilities.database.Database
import cats.effect.unsafe.implicits.global
import cats.effect.{Async, IO, IOApp, Resource}
import cats.syntax.all._
import domain.auth._
import domain.errors._
import domain.salesforce._
import fs2.Stream
import interpreters.http._
import interpreters.repos._
import interpreters.AccessTokensInterpreter
import algebras.repos.MailMessageRepo
import org.http4s.blaze.client.BlazeClientBuilder
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import programs.{SalesforceProgram, WrapperProgram}
import pureconfig.ConfigSource
import services._

import scala.concurrent.duration._
import cats.Parallel

object Setup:

  def setupWrapper[F[_]: Async]: Stream[F, (SalesforceMailService[F], SalesforceSmsService[F], SalesforceErrorHandlerService[F], SendDefinitionService[F])] =
    for
      config <- Stream.eval(Configuration.load[F](ConfigSource.file("application.conf")))
      given Configuration[F] = config
      database <- Stream.resource(Database.make[F])
      given Database[F] = database
      logger <- Stream(Slf4jLogger.getLogger[F])
      given SelfAwareStructuredLogger[F] = logger
      client <- Stream.resource(BlazeClientBuilder[F](global.compute).resource)
      authClient <- Stream(AuthClientInterpreter(client))
      authRepo <- Stream(AuthRepoInterpreter.apply)
      authService <- Stream(AuthService.make(authRepo, authClient))
      accessTokens <- Stream(AccessTokensInterpreter(authService))
      postClient <- Stream(PostClientInterpreter(client, "baseurl", accessTokens))
      getClient <- Stream(GetClientInterpreter(client, "baseurl", accessTokens))
      mailMessageRepo <- Stream(MailMessageRepoInterpreter.apply)
      smsMessageRepo <- Stream(SmsMessageRepoInterpreter.apply)
      mailMessageService <- Stream(MailMessageService.make(mailMessageRepo))
      sendDefinitionRepo <- Stream(SendDefinitionRepoInterpreter.apply)
      isTest <- Stream.eval(authRepo.getTestSettings)
      salesforceMailService <- Stream(SalesforceMailService.make(postClient, mailMessageService, isTest))
      salesforceSmsService <- Stream(SalesforceSmsService.make(postClient, smsMessageRepo, isTest))
      salesforceErrorHandlerService <- Stream(SalesforceErrorHandlerService.make(smsMessageRepo, mailMessageService, getClient))
      salesforceSendDefinitionService <- Stream(SendDefinitionService.make(sendDefinitionRepo, postClient))
    yield (salesforceMailService, salesforceSmsService, salesforceErrorHandlerService, salesforceSendDefinitionService)

  def wrapperProgram[F[_]: Async]: Stream[F, Unit] =
    setupWrapper.flatMap { case (mailService, smsService, salesforceErrorHandlerService, salesforceSendDefinitionService) =>
      WrapperProgram(mailService, smsService, salesforceErrorHandlerService, salesforceSendDefinitionService).process
    }

  def wrapperProgramTest[F[_]: Async]: Stream[F, Unit] =
    setupWrapper.flatMap { case (mailService, smsService, salesforceErrorHandlerService, salesforceSendDefinitionService) =>
      salesforceErrorHandlerService.run
    }

object Mail extends IOApp.Simple:

  import Setup._

  def run: IO[Unit] =
    setupWrapper[IO].flatMap { case (mailService, smsService, salesforceErrorHandlerService, salesforceSendDefinitionService) =>
      mailService.fetchAndPushMails ++ Stream.eval(salesforceErrorHandlerService.handleMailsInError)
    }.compile.drain

object SMS extends IOApp.Simple:

  import Setup._

  def run: IO[Unit] =
    setupWrapper[IO].flatMap { case (mailService, smsService, salesforceErrorHandlerService, salesforceSendDefinitionService) =>
      smsService.fetchAndPushSms ++ Stream.eval(salesforceErrorHandlerService.handleSmsInError)
    }.compile.drain

object SendDefinition extends IOApp.Simple:

  import Setup._

  def run: IO[Unit] =
    setupWrapper[IO].flatMap { case (mailService, smsService, salesforceErrorHandlerService, salesforceSendDefinitionService) =>
      Stream.eval(salesforceSendDefinitionService.handlePendingSendDefinitions)
    }.compile.drain


