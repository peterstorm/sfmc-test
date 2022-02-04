package programs

import cats.effect.Async
import cats.syntax.all._
import fs2.Stream
import services.{SalesforceMailService, SalesforceSmsService, SalesforceErrorHandlerService, SendDefinitionService}

import scala.concurrent.duration._

final case class WrapperProgram[F[_]: Async](
  salesforceMailService: SalesforceMailService[F],
  salesforceSmsService: SalesforceSmsService[F],
  salesforceErrorHandlerService: SalesforceErrorHandlerService[F],
  salesforceSendDefinitionService: SendDefinitionService[F]
):

  def process: Stream[F, Unit] =
      salesforceMailService.fetchAndPushMails ++
      salesforceSmsService.fetchAndPushSms ++
      salesforceErrorHandlerService.run
      //Stream.eval(salesforceSendDefinitionService.handlePendingSendDefinitions)
