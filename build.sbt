lazy val root =
  (project in file("."))
    .settings(name := "pescar")
    .settings(version := "0.1")
    .settings(skip in publish := true)
    .aggregate(
      analyticsApi,
      analyticsConsumer,
      config,
      io,
      time,
      utilsStd,
      utilsZio,
    )

val commonSettings =
  Def.settings(
    scalacOptions := ScalaOptions.dev,
    scalaVersion := "2.13.1",
  )

lazy val analyticsApi =
  (project in file("analytics.api"))
    .settings(name := "analytics.api")
    .settings(commonSettings)
    .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
    .settings(fork in Test := true)
    .settings(libraryDependencies := Dependencies.analyticsApi.toSeq)
    .dependsOn(
      io % Test,
      time,
      utilsStd,
      utilsZio,
      analyticsConsumer,// TODO remove
    )

lazy val analyticsConsumer =
  (project in file("analytics.consumer"))
    .settings(name := "analytics.consumer")
    .settings(commonSettings)
    .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
    .settings(fork in Test := true)
    .settings(libraryDependencies := Dependencies.analyticsConsumer.toSeq)
    .dependsOn(
      config,
      io,
      time,
      utilsStd,
      utilsZio,
    )

lazy val config =
  (project in file("config"))
    .settings(name := "config")
    .settings(commonSettings)
    .settings(libraryDependencies := Dependencies.config.toSeq)
    .dependsOn(
      io,
    )

lazy val io =
  (project in file("io"))
    .settings(name := "io")
    .settings(commonSettings)
    .settings(libraryDependencies := Dependencies.io.toSeq)

lazy val time =
  (project in file("time"))
    .settings(name := "time")
    .settings(commonSettings)

lazy val utilsStd =
  (project in file("utils.std"))
    .settings(name := "utils.std")
    .settings(commonSettings)

lazy val utilsZio =
  (project in file("utils.zio"))
    .settings(name := "utils.zio")
    .settings(commonSettings)
    .settings(libraryDependencies := Dependencies.utilsZio.toSeq)

//Revolver.settings
