name := "analytics"

version := "0.1"

scalaVersion := "2.13.1"

fork in Test := true // OutOfMemory Metaspace

scalacOptions ++= Seq(
  "-deprecation",                  // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",            // Specify character encoding used by source files.
  "-explaintypes",                 // Explain type errors in more detail.
  "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds",         // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
//  "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
//  "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
  "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
  "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",       // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",        // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",              // Warn when dead code is identified.
  "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",          // Warn when numerics are widened.
  "-Ywarn-macros:after",           // Because implicits were pointed as unused.
  "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",          // Warn if a local definition is unused.
  "-Ywarn-unused:params",          // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",        // Warn if a private member is unused.
  "-Ywarn-value-discard"           // Warn when non-Unit expression results are unused.
)

//scalacOptions += "-Ylog-classpath"

libraryDependencies += "com.github.ghostdogpr"        %% "caliban"         % "0.5.0"
libraryDependencies += "com.github.ghostdogpr"        %% "caliban-http4s"  % "0.5.0"
libraryDependencies += "com.github.pureconfig"        %% "pureconfig"      % "0.12.2"
libraryDependencies += "com.github.pureconfig"        %% "pureconfig-yaml" % "0.12.2"
libraryDependencies += "dev.zio"                      %% "zio"             % "1.0.0-RC17"
libraryDependencies += "dev.zio"                      %% "zio-streams"     % "1.0.0-RC17"
libraryDependencies += "dev.zio"                      %% "zio-test"        % "1.0.0-RC17" % "test"
libraryDependencies += "dev.zio"                      %% "zio-test-sbt"    % "1.0.0-RC17" % "test"
libraryDependencies += "io.circe"                     %% "circe-core"      % "0.12.3"
libraryDependencies += "io.circe"                     %% "circe-generic"   % "0.12.3"
libraryDependencies += "io.circe"                     %% "circe-literal"   % "0.12.3"
libraryDependencies += "io.circe"                     %% "circe-parser"    % "0.12.3"

// gob_api_consumer dependencies
libraryDependencies += "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % "2.0.0-RC5"
libraryDependencies += "com.softwaremill.sttp.client" %% "circe"                         % "2.0.0-RC5"
libraryDependencies += "com.softwaremill.sttp.client" %% "core"                          % "2.0.0-RC5"
libraryDependencies += "dev.zio"                      %% "zio-nio"                       % "0.4.0"
libraryDependencies += "org.http4s"                   %% "http4s-blaze-client"           % "0.21.0-M5"
libraryDependencies += "org.http4s"                   %% "http4s-dsl"                    % "0.21.0-M5"
libraryDependencies += "org.tpolecat"                 %% "doobie-core"                   % "0.8.6"
libraryDependencies += "org.tpolecat"                 %% "doobie-hikari"                 % "0.8.6"
libraryDependencies += "org.tpolecat"                 %% "doobie-postgres"               % "0.8.6"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

Revolver.settings
