package config

import io.file.openResource
import pureconfig.module.yaml.YamlConfigSource
import zio.ZIO

object ConfigLoader {

  val loadYamlConfig = openResource("/conf.yaml") >>= loadYamlFromString

  private final def loadYamlFromString(confString: String) = {
    import pureconfig.generic.auto._
    ZIO
      .fromEither(YamlConfigSource.string(confString).load[Config])
      .mapError(ConfigFailure.fromConfigReaderFailures)
  }

}
