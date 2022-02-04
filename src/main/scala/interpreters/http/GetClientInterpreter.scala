package interpreters.http

import algebras.http.GetClient
import algebras.AccessTokens
import cats.effect.Concurrent
import cats.syntax.all._
// this import is to not have to do .widen[SentStatus]
import cats.conversions.all._
import domain.auth.AccessToken
import domain.errors._
import domain.salesforce.SentStatus
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.Client
import org.http4s.headers._
import org.typelevel.log4cats.Logger
import io.circe.generic.auto._

class GetClientInterpreter[F[_]: Concurrent: Logger](
  client: Client[F],
  baseUrl: String,
  accessTokens: AccessTokens[F]
) extends GetClient[F]:

  def getEmailStatus(uuid: String): F[SentStatus] =
    accessTokens.withAccessToken( accessToken =>
      val request = Request[F](
        Method.GET,
        Uri.unsafeFromString(s"${baseUrl}/messaging/v1/email/messages/${uuid}"),
        headers = Headers(
          Accept(MediaType.application.json),
          Authorization(Credentials.Token(AuthScheme.Bearer, accessToken.access_token))
        )
      )
      runRequest(request)
    )

  def getSmsStatus(uuid: String): F[SentStatus] =
    accessTokens.withAccessToken( accessToken =>
      val request = Request[F](
        Method.GET,
        Uri.unsafeFromString(s"${baseUrl}/messaging/v1/sms/messages/${uuid}"),
        headers = Headers(
          Accept(MediaType.application.json),
          Authorization(Credentials.Token(AuthScheme.Bearer, accessToken.access_token))
        )
      )
      runRequest(request)
    )

  private def runRequest(request: Request[F]): F[SentStatus] =
    client.run(request).use( resp =>
      resp.status match
        case Status.Ok => resp.as[SentStatus.SentStatusDTO]
        case Status.NotFound => resp.as[SentStatus.SentStatusError]
        case Status.Unauthorized => raiseError(UnauthorizedError.apply, resp)
        case Status.Forbidden => raiseError(ForbiddenError.apply, resp)
        case Status.BadRequest => raiseError(BadRequestError.apply, resp)
        case _         => raiseError(HttpError.apply, resp)
    )

  private def responseBody(resp: Response[F]): F[String] =
    resp.bodyText.compile.foldMonoid

  private def raiseError[A](error: String => Throwable, resp: Response[F]): F[A] =
    responseBody(resp) >>=
      (body => Concurrent[F].raiseError[A](error(s"Status: ${resp.status.code}, $body")))

object GetClientInterpreter:

  def apply[F[_]: Concurrent: Logger](
    client: Client[F],
    baseUrl: String,
    accessTokens: AccessTokens[F]
  ): GetClient[F] =
    new GetClientInterpreter(client, baseUrl, accessTokens)
