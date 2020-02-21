package api

import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }

object TestMain
    extends DefaultRunnableSpec(
      suite("graphql api")(
        testM("landings and summaries filtered") {
          aux runTestCase "landings and summaries filtered"
        },
        testM("landings for one month") {
          aux runTestCase "landings for one month"
        },
        testM("landings for several months including an empty one") {
          aux runTestCase "landings for several months including an empty one"
        },
        testM("landings summaries for several months including an empty one") {
          aux runTestCase "landings summaries for several months including an empty one"
        },
        testM("landings summary for one month") {
          aux runTestCase "landings summary for one month"
        },
      )
    )

object aux {

  import io.circe.parser.parse
  import io.circe.syntax._

  // TODO diffson
  def runTestCase(name: String) =
    for {
      userDir                 <- zio.system.property("user.dir").someOrFailException.provide(zio.system.System.Live)
      path                    = userDir ++ "/src/test/scala/api/cases"
      fileName                = name replace (' ', '_')
      loadAndExecuteQuery     = io.file.open(s"$path/$fileName.graphql") flatMap (Main.api.interpreter.execute(_))
      loadAndParseExpected    = io.file.open(s"$path/$fileName.json") flatMap (ZIO fromEither parse(_))
      (gqlResponse, expected) <- loadAndExecuteQuery zipPar loadAndParseExpected
    } yield assert(gqlResponse.asJson, equalTo(expected))

}
