package config

import pureconfig.module.yaml.YamlConfigSource
import zio.{ UIO, ZIO }

object ConfigLoader {

  val test: UIO[Config] = ZIO succeed Config(
    DBConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://postgres:5432/test_datos_gob",
      "palan",
      "",
      32
    ),
    ServerConfig(
      "",
      0
    )
  )

  final val loadYamlConfig = io.file.openResource("/conf.yaml") >>= loadYamlFromString

  private final def loadYamlFromString(confString: String) = {
    import pureconfig.generic.auto._
    ZIO
      .fromEither(YamlConfigSource.string(confString).load[Config])
      .mapError(ConfigFailure.fromConfigReaderFailures)
  }

}
