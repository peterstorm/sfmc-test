package algebras.http

import domain.auth.AccessToken
import domain.salesforce._

trait PostClient[F[_]]:

  def postEmail(dto: MessageDTO, uuid: String): F[MessageResponseDTO]

  def postSms(dto: SmsMessageDTO, uuid: String): F[MessageResponseDTO]

  def postSendDefinition(
    dto: SendDefinitionDTO
  ): F[SendDefinitionResponseDTO]
