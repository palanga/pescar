import sbt.librarymanagement.syntax.{ stringToOrganization, Test }

object Dependencies {

  import Definitions._

  private val common = Set(
    logbackClassic,
    zio,
    zioStreams,
    zioTest,
    zioTestSbt,
  )

  val analyticsApi = common ++ Set(
    caliban,
    calibanHttp4s,
    circeCore   % Test,
    circeParser % Test,
  )

  val config = Set(
    pureconfig,
    pureconfigYaml,
    zio,
  )

  val analyticsConsumer = common ++ Set(
    circeCore,
    circeGeneric,
    circeParser,
    doobieCore,
    doobieHikari,
    doobiePostgres,
    http4sClient,
    http4sCirce,
    sttpAsyncBackendZio,
    sttpCirce,
    sttpCore,
  )

  val io = Set(
    zio,
//    zioMacros,
    zioNio,
    zioStreams,
  )

  val utilsZio = Set(
    doobieCore,
    zio,
    zioInteropCats,
    zioStreams,
  )

}

object Definitions {

  val caliban       = "com.github.ghostdogpr" %% "caliban"        % Versions.caliban
  val calibanHttp4s = "com.github.ghostdogpr" %% "caliban-http4s" % Versions.caliban

  val circeCore    = "io.circe" %% "circe-core"    % Versions.circe
  val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe
  val circeParser  = "io.circe" %% "circe-parser"  % Versions.circe

  val doobieCore     = "org.tpolecat" %% "doobie-core"     % Versions.doobie
  val doobieHikari   = "org.tpolecat" %% "doobie-hikari"   % Versions.doobie
  val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % Versions.doobie

  val http4sClient = "org.http4s" %% "http4s-blaze-client" % Versions.http4s
  val http4sCirce  = "org.http4s" %% "http4s-circe"        % Versions.http4s
  val http4sDsl    = "org.http4s" %% "http4s-dsl"          % Versions.http4s

  val logbackClassic = "ch.qos.logback" % "logback-classic" % Versions.logback

  val pureconfig     = "com.github.pureconfig" %% "pureconfig"      % Versions.pureconfig
  val pureconfigYaml = "com.github.pureconfig" %% "pureconfig-yaml" % Versions.pureconfig

  val sttpAsyncBackendZio = "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % Versions.sttp
  val sttpCirce           = "com.softwaremill.sttp.client" %% "circe"                         % Versions.sttp
  val sttpCore            = "com.softwaremill.sttp.client" %% "core"                          % Versions.sttp

  val zio        = "dev.zio" %% "zio"          % Versions.zio
  val zioMacros  = "dev.zio" %% "zio-macros"   % Versions.zio
  val zioStreams = "dev.zio" %% "zio-streams"  % Versions.zio
  val zioTest    = "dev.zio" %% "zio-test"     % Versions.zio % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % Versions.zio % Test

  val zioInteropCats = "dev.zio" %% "zio-interop-cats" % Versions.zioInteropCats

  val zioNio = "dev.zio" %% "zio-nio" % Versions.zioNio

}

object Versions {
  val caliban        = "0.7.4"
  val circe          = "0.13.0"
  val doobie         = "0.8.6"
  val http4s         = "0.21.1"
  val logback        = "1.2.3"
  val pureconfig     = "0.12.2"
  val sttp           = "2.0.6"
  val zio            = "1.0.0-RC18-2"
  val zioInteropCats = "2.0.0.0-RC12"
  val zioNio         = "1.0.0-RC6"
}
