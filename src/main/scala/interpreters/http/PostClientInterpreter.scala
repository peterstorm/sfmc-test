package interpreters.http

import algebras.http.PostClient
import algebras.AccessTokens
import cats.effect.Concurrent
import cats.syntax.all._
import domain.auth.AccessToken
import domain.errors._
import domain.salesforce._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import io.circe.Decoder



class PostClientInterpreter[F[_]: Concurrent](
  client: Client[F],
  baseUrl: String,
  accessTokens: AccessTokens[F]
) extends PostClient[F]:

  def postEmail(dto: MessageDTO, uuid: String): F[MessageResponseDTO] =
    accessTokens.withAccessToken( accessToken =>
      val request = Request[F](
        Method.POST,
        Uri.unsafeFromString(s"${baseUrl}/messaging/v1/email/messages/${uuid}"),
        headers = Headers(
          `Content-Type`(MediaType.application.json),
          Accept(MediaType.application.json),
          Authorization(Credentials.Token(AuthScheme.Bearer, accessToken.access_token))
        )
      ).withEntity(dto)
      client.run(request).use( resp =>
        handleResponse[MessageResponseDTO](resp)
      )
    )

  def postSms(dto: SmsMessageDTO, uuid: String): F[MessageResponseDTO] =
    accessTokens.withAccessToken( accessToken =>
      val request = Request[F](
        Method.POST,
        Uri.unsafeFromString(s"${baseUrl}/messaging/v1/sms/messages/${uuid}"),
        headers = Headers(
          `Content-Type`(MediaType.application.json),
          Accept(MediaType.application.json),
          Authorization(Credentials.Token(AuthScheme.Bearer, accessToken.access_token))
        )
      ).withEntity(dto)
      client.run(request).use( resp =>
        handleResponse[MessageResponseDTO](resp)
      )
    )

  def postSendDefinition(
    dto: SendDefinitionDTO
  ): F[SendDefinitionResponseDTO] =
    accessTokens.withAccessToken( accessToken =>
      val request = Request[F](
        Method.POST,
        Uri.unsafeFromString(s"${baseUrl}/messaging/v1/sms/definitions"),
        headers = Headers(
          `Content-Type`(MediaType.application.json),
          Accept(MediaType.application.json),
          Authorization(Credentials.Token(AuthScheme.Bearer, accessToken.access_token))
        )
      ).withEntity(dto)
      client.run(request).use( resp =>
        handleResponse[SendDefinitionResponseDTO](resp)
      )
    )

  private def handleResponse[A: Decoder](resp: Response[F]): F[A] =
      resp.status match
        case Status.Accepted => resp.as[A]
        case Status.Unauthorized => raiseError(UnauthorizedError.apply, resp)
        case Status.Forbidden => raiseError(ForbiddenError.apply, resp)
        case _         => raiseError(HttpError.apply, resp)

  private def responseBody(resp: Response[F]): F[String] =
    resp.bodyText.compile.foldMonoid

  private def raiseError[A](error: String => Throwable, resp: Response[F]): F[A] =
    responseBody(resp) >>= 
      (body => Concurrent[F].raiseError[A](error(s"Status: ${resp.status.code}, $body")))

object PostClientInterpreter:

  def apply[F[_]: Concurrent](
    client: Client[F],
    baseUrl: String,
    accessTokens: AccessTokens[F]
  ): PostClient[F] =
    new PostClientInterpreter(client, baseUrl, accessTokens)
