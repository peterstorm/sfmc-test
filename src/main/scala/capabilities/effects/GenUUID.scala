package capabilities.effects

import cats.effect.Sync
import cats.syntax.all._
import java.util.UUID

trait GenUUID[F[_]]:
    def make: F[UUID]

object GenUUID:
    def apply[F[_]](using ev: GenUUID[F]): GenUUID[F] = ev

    given syncGenUUID[F[_]](using syncF: Sync[F]): GenUUID[F] with
        def make: F[UUID] = syncF.delay(UUID.randomUUID())


