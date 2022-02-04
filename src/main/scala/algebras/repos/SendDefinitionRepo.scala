package algebras.repos

import domain.salesforce.SendDefinition

trait SendDefinitionRepo[F[_]]:

  def getSendDefinitions: F[List[SendDefinition]]

  def setSendDefinitionToHandled(
    sendDefinition: SendDefinition,
    status: Int,
    message: String
  ): F[Unit]

