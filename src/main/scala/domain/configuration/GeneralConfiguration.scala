package domain.configuration

import pureconfig._

final case class GeneralConfiguration(
  processRerun: String
)

object GeneralConfiguration:

  given ConfigReader[GeneralConfiguration] =
    ConfigReader.forProduct1("processRerun")(GeneralConfiguration(_))
