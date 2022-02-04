package algebras.http

import domain.salesforce._

trait GetClient[F[_]]:

  def getEmailStatus(uuid: String): F[SentStatus]

  def getSmsStatus(uuid: String): F[SentStatus]

