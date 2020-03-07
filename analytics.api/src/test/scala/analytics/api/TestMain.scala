package analytics.api

import analytics.api.Main.api
import io.file.open
import zio.system.System
import zio.test.Assertion.equalTo
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }
import zio.{ system, ZIO }

object TestMain
    extends DefaultRunnableSpec(
      suite("analytics graphql api")(
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

  import io.circe.parser.{ parse => circeParse }
  import io.circe.syntax._

  // TODO convert to string in order to benefit from string diffing in next ZIO release
  def runTestCase(name: String) =
    for {
      userDir                 <- system.property("user.dir").someOrFailException.provide(System.Live)
      path                    = userDir ++ "/src/test/scala/analytics/api/cases"
      pathFromIntelliJ        = userDir ++ "/analytics.api/src/test/scala/analytics/api/cases"
      fileName                = name replace (' ', '_')
      query                   = open(s"$path/$fileName.graphql") race open(s"$pathFromIntelliJ/$fileName.graphql")
      expected                = open(s"$path/$fileName.json") race open(s"$pathFromIntelliJ/$fileName.json")
      (gqlResponse, expected) <- (query >>= (api.interpreter.execute(_))) zipPar (expected >>= parse)
    } yield assert(gqlResponse.asJson, equalTo(expected))

  private def parse(str: String) = ZIO fromEither circeParse(str)

}
