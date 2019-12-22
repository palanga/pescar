package thescientist.config

import java.net.URLDecoder
import java.nio.file.{ Path, Paths }

import pureconfig.ConfigReader
import pureconfig.generic.auto._
import pureconfig.module.yaml._
import zio.{ Task, UIO, ZIO }

object ConfigLoader {

  val default: UIO[Config] = ZIO succeed Config(
    DBConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://postgres:5432/postgres",
      "postgres",
      "postgres",
      32
    ),
    ServerConfig(
      "",
      0
    )
  )

  final val loadYamlConfig: Task[Config] = resourcePath("/conf.yaml") >>= loadYamlFromPath[Config]

  private final def resourcePath(path: String): Task[Path] =
    ZIO
      .effect(this.getClass.getResource(path).getFile)
      .map(URLDecoder.decode(_, "UTF-8"))
      .map(Paths.get(_))

  private final def loadYamlFromPath[T](path: Path)(implicit cr: ConfigReader[T]): Task[T] =
    ZIO
      .fromEither(YamlConfigSource.file(path).load[T])
      .mapError(configReaderFailures => new Exception(configReaderFailures.toString))

}
