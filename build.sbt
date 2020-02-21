lazy val root = project
  .in(file("."))
  .settings(name := "analytics")
  .settings(version := "0.1")
  .settings(skip in publish := true)
  .aggregate(api, config, core, io, time, utils, zioUtils)

val commonSettings = Def.settings(
  scalaVersion := "2.13.1",
  scalacOptions := ScalaOptions.dev
)

lazy val api =
  project
    .in(file("api"))
    .settings(name := "api")
    .settings(commonSettings)
    .settings(libraryDependencies := Dependencies.api.toSeq)
    .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
    .settings(fork in Test := true)
    .dependsOn(time, zioUtils, utils)
    .dependsOn(io)// TODO just for test

lazy val config =
  project
    .in(file("config"))
    .settings(name := "config")
    .settings(commonSettings)
    .settings(libraryDependencies := Dependencies.config.toSeq)
    .dependsOn(io)

lazy val core =
  project
    .in(file("core"))
    .settings(name := "core")
    .settings(commonSettings)
    .settings(libraryDependencies := (Dependencies.api ++ Dependencies.consumer).toSeq)
    .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
    .settings(fork in Test := true)
    .dependsOn(config, io, time, utils, zioUtils)

lazy val io =
  project
    .in(file("io"))
    .settings(name := "io")
    .settings(commonSettings)
    .settings(libraryDependencies := Dependencies.io.toSeq)
    .dependsOn(utils)

lazy val time =
  project
    .in(file("time"))
    .settings(name := "time")
    .settings(commonSettings)

lazy val utils =
  project
    .in(file("utils"))
    .settings(name := "utils")
    .settings(commonSettings)

lazy val zioUtils =
  project
    .in(file("zio-utils"))
    .settings(name := "utils.zio")
    .settings(commonSettings)
    .settings(libraryDependencies := Dependencies.zioUtils.toSeq)

//Revolver.settings
