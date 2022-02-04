package interpreters.http

import org.http4s.client.Client
import org.http4s._
import org.http4s.headers._
import algebras.http.AuthClient
import domain.auth._
import cats.effect.Concurrent
import cats.syntax.all._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import domain.errors.HttpError

class AuthClientInterpreter[F[_]: Concurrent](
  client: Client[F]
) extends AuthClient[F]:

  def renewAccessToken(credentials: ClientCredentials): F[AccessToken] =
    val request = Request[F](
      Method.POST,
      Uri.unsafeFromString(
        "https://mc7grq3x7g6lvy1qf786bj5ts0q8.auth.marketingcloudapis.com/v2/token"
      ),
      headers = Headers(
        `Content-Type`(MediaType.application.json),
        Accept(MediaType.application.json)
      )
    )
    .withEntity(
      AuthDTO(
        "client_credentials",
        credentials.clientId.value,
        credentials.clientSecret.value,
        "email_read email_write email_send sms_send sms_read sms_write"
      )
    )
    client.run(request).use( resp =>
      resp.status match
        case Status.Ok => resp.as[AccessToken]
        case _         => resp.as[String].flatMap(s => HttpError(s).raiseError[F, AccessToken])
    )

object AuthClientInterpreter:

  def apply[F[_]: Concurrent](
    client: Client[F]
  ): AuthClient[F] =
    new AuthClientInterpreter(client)

