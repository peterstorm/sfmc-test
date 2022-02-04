package domain.configuration

import pureconfig._

final case class DatabaseConfiguration(
  driver: String,
  url: String,
  username: String,
  password: String
)

object DatabaseConfiguration:

  given ConfigReader[DatabaseConfiguration] =
    ConfigReader.forProduct4("driver", "url", "username", "password")(DatabaseConfiguration(_,_,_,_))
