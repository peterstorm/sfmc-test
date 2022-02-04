package domain.errors

import pureconfig.error.ConfigReaderFailures
import cats.syntax.all._

final case class ConfigLoadError(
  failures: ConfigReaderFailures
) extends BaseError(show"Configuration load failed with: ${failures.toList.map(_.description).mkString(", ")}")


