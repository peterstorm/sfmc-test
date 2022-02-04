package domain.configuration

import pureconfig._

final case class AppConfig(
  database: DatabaseConfiguration,
  general: GeneralConfiguration
)

object AppConfig:

  given ConfigReader[AppConfig] =
    ConfigReader.forProduct2("database", "general")(AppConfig(_,_))
