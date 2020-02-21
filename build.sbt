name := "analytics"

version := "0.1"

scalaVersion := "2.13.1"

fork in Test := true // OutOfMemory Metaspace

scalacOptions := ScalaOptions.dev

libraryDependencies := (Dependencies.api ++ Dependencies.consumer).toSeq

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

Revolver.settings
