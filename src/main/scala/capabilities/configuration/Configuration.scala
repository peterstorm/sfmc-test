package capabilities.configuration

import domain.configuration.AppConfig
import domain.errors.ConfigLoadError
import cats.effect.Sync
import cats.syntax.all._
import pureconfig.ConfigSource

trait Configuration[F[_]]:

  def access: F[AppConfig]

object Configuration:

  def apply[F[_]](using ev: Configuration[F]): Configuration[F] = ev

  def load[F[_]: Sync](configSource: ConfigSource): F[Configuration[F]] = process(configSource)

  def process[F[_]: Sync](source: ConfigSource): F[Configuration[F]] =
    Sync[F].fromEither(source.load[AppConfig].leftMap(ConfigLoadError.apply)).map( config =>
        new Configuration[F]:
          def access: F[AppConfig] = Sync[F].delay(config)
    )

