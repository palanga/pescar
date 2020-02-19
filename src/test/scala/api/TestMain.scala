package api

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }

object TestMain
    extends DefaultRunnableSpec(
      suite("graphql api")(
        List(
          "landings and summaries filtered",
          "landings for one month",
          "landings for several months including an empty one",
          "landings summaries for several months including an empty one",
          "landings summary for one month",
        ).map(aux.buildTestCase): _*
      )
    )

object aux {

  import io.circe.parser.parse
  import io.circe.syntax._

  def buildTestCase(name: String) = testM(name) { aux runTestCase name.replace(' ', '_') }

  // TODO diffson
  private def runTestCase(name: String) =
    for {
      userDir                 <- zio.system.property("user.dir").someOrFailException.provide(zio.system.System.Live)
      path                    = userDir ++ "/src/test/scala/api/cases"
      loadAndExecuteQuery     = io.file.open(s"$path/$name.graphql") flatMap (Main.api.interpreter.execute(_))
      loadAndParseExpected    = io.file.open(s"$path/$name.json") flatMap (ZIO fromEither parse(_))
      (gqlResponse, expected) <- loadAndExecuteQuery zipPar loadAndParseExpected
    } yield assert(gqlResponse.asJson, equalTo(expected))

}
