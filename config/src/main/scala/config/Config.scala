package config

import config.types._

case class Config(db: DBConfig, server: ServerConfig)

case class DBConfig(
  driverName: DriverName,
  url: URL,
  username: Username,
  password: Password,
  connectionThreadPoolSize: ThreadPoolSize,
)

case class ServerConfig(host: Hostname, port: PortNumber)

object Config {

  val test =
    Config(
      DBConfig(
        "org.postgresql.Driver",
        "jdbc:postgresql://postgres:5432/test_datos_gob",
        "palan",
        "",
        32,
      ),
      ServerConfig(
        "",
        0,
      ),
    )

}
