lazy val root = project
  .in(file("."))
  .settings(name := "analytics")
  .settings(version := "0.1")
  .settings(skip in publish := true)
  .aggregate(core, time)

val commonSettings = Def.settings(
  scalaVersion := "2.13.1",
  scalacOptions := ScalaOptions.dev
)

lazy val core =
  project
    .in(file("core"))
    .settings(name := "core")
    .settings(commonSettings)
    .settings(libraryDependencies := (Dependencies.api ++ Dependencies.consumer).toSeq)
    .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
    .settings(fork in Test := true)
    .dependsOn(time, utils)

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

//Revolver.settings
