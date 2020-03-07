package config

import io.file
import pureconfig.module.yaml.YamlConfigSource
import zio.ZIO
import zio.system.property

object ConfigLoader {

  val loadYamlConfig =
    property("user.dir").someOrFailException
      .flatMap(file.open _ compose (_ ++ "/consumer/conf.yaml"))// TODO
      .flatMap(loadYamlFromString)

  private final def loadYamlFromString(file: String) = {
    import pureconfig.generic.auto._
    ZIO
      .effect(YamlConfigSource.string(file).load[Config].left.map(ConfigFailure.fromConfigReaderFailures))
      .flatMap(ZIO fromEither _)
  }

}
