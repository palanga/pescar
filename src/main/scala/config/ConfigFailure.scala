package config

import pureconfig.error.ConfigReaderFailures

case class ConfigFailure(message: String) extends Exception(message)

object ConfigFailure {
  def fromConfigReaderFailures(failures: ConfigReaderFailures) =
    ConfigFailure(
      "\n" ++ failures.toList
        .map(f => s"${f.description}${f.location.fold("")(" " ++ _.description)} ${f.toString}")
        .mkString("\n")
    )
}
