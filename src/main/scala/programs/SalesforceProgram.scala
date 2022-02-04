package programs

import cats.effect.Async
import cats.syntax.all._
import fs2.Stream
import services.{SalesforceMailService, SalesforceSmsService, TokenCache}

import scala.concurrent.duration._

final case class SalesforceProgram[F[_]: Async](
  cache: TokenCache[F],
  salesforceMailService: SalesforceMailService[F],
  salesforceSmsService: SalesforceSmsService[F]
):

  def process: Stream[F, Unit] =
    Stream(
      Stream(()).repeat.meteredStartImmediately(30.seconds) >> salesforceMailService.fetchAndPushMails,
      Stream(()).repeat.meteredStartImmediately(30.seconds) >> salesforceSmsService.fetchAndPushSms,
      Stream.eval(cache.expire)
    ).parJoinUnbounded
