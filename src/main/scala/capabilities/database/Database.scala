package capabilities.database

import cats.effect.{Async, Resource}
import cats.syntax.all._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import capabilities.configuration.Configuration

trait Database[F[_]]:

    def transactor: HikariTransactor[F]

object Database:

    def apply[F[_]](using ev: Database[F]): Database[F] = ev

    def make[F[_]: Configuration: Async]: Resource[F, Database[F]] =
        for
            config <- Resource.eval(Configuration[F].access.map(_.database))
            conExContext <- ExecutionContexts.fixedThreadPool[F](32)
            hikari <- HikariTransactor.newHikariTransactor(
                config.driver,
                config.url,
                config.username,
                config.password,
                conExContext
            )
        yield (
            new Database[F]:
                def transactor: HikariTransactor[F] = hikari
        )
