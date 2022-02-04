package interpreters.repos

import cats.effect.MonadCancel
import cats.syntax.all._
import capabilities.database.Database
import domain.salesforce.SendDefinition
import algebras.repos.SendDefinitionRepo
import doobie.util.query.Query0
import doobie.syntax.all._
import doobie.implicits._
import doobie.util.update.Update0
import java.time.format.DateTimeFormatter

class SendDefinitionRepoInterpreter[F[_]](
  using F: MonadCancel[F, Throwable], DB: Database[F]
) extends SendDefinitionRepo[F]:

  val xa = Database[F].transactor
  import SendDefinitionSQL._

  def getSendDefinitions: F[List[SendDefinition]] =
    getSendDefinitionsSQL.to[List].transact(xa)

  def setSendDefinitionToHandled(
    sendDefinition: SendDefinition,
    status: Int,
    message: String
  ): F[Unit] =
    setSendDefinitionToHandledSQL(sendDefinition, status, message).run.transact(xa).void

object SendDefinitionRepoInterpreter:

  def apply[F[_]](
    using F: MonadCancel[F, Throwable], DB: Database[F]
  ): SendDefinitionRepoInterpreter[F] =
    new SendDefinitionRepoInterpreter

object SendDefinitionSQL:

  def getSendDefinitionsSQL: Query0[SendDefinition] = sql"""
    SELECT * FROM SFMC_SEND_DEFINITION
    WHERE STATUS_ID = 0
    """.query[SendDefinition]

  def setSendDefinitionToHandledSQL(sendDefinition: SendDefinition, status: Int, message: String): Update0 = sql"""
    UPDATE SFMC_SEND_DEFINITION
    SET STATUS_ID = $status, MESSAGE = $message
    WHERE ID = ${sendDefinition.id}
    """.update
