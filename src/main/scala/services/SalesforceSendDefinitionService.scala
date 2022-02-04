package services

import org.typelevel.log4cats.Logger
import algebras.repos.SendDefinitionRepo
import domain.salesforce.{SendDefinition, SendDefinitionDTO, SendDefinitionContent, SendDefinitionSubscriptions}
import cats.syntax.all._
import algebras.http.PostClient
import cats.MonadThrow

trait SendDefinitionService[F[_]]:

  def handlePendingSendDefinitions: F[Unit]

object SendDefinitionService:

  def make[F[_]: Logger: MonadThrow](
    sendDefinitionRepo: SendDefinitionRepo[F],
    postClient: PostClient[F]
  ): SendDefinitionService[F] =

    new SendDefinitionService[F]:

      def handlePendingSendDefinitions: F[Unit] =
        sendDefinitionRepo.getSendDefinitions
          .flatMap( list =>
            list.traverse( sendDefinition =>
              postClient.postSendDefinition(
                SendDefinitionDTO(
                  sendDefinition.name,
                  sendDefinition.name,
                  sendDefinition.name,
                  SendDefinitionContent("This is a test"),
                  SendDefinitionSubscriptions(
                    "46777000000",
                    "FOO",
                    None,
                    None,
                    None
                  )
                )
              )
              .attempt
              .flatMap( either =>
                either match
                  case Right(resp) =>
                    Logger[F].info(s"SendDefintion created with response: ${resp.toString}") >>
                    sendDefinitionRepo.setSendDefinitionToHandled(sendDefinition, 1, resp.toString)
                  case Left(error) =>
                    Logger[F].error(s"SendDefintion creation failed with: ${error.getMessage}") >>
                    sendDefinitionRepo.setSendDefinitionToHandled(sendDefinition, 3, error.getMessage)
              )
            )
          ).void
