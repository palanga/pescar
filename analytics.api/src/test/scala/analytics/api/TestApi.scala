package analytics.api

import analytics.api.Main.api
import analytics.api.database.landings.{ module => db }
import io.circe.parser.{ parse => circeParse }
import io.file.open
import zio.system.System
import zio.test.Assertion.equalTo
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }
import zio.{ system, ZEnv, ZIO }

object TestApi extends DefaultRunnableSpec {

  // TODO no se si esto funciona como espero pero paja
  private val memoizedInterpreter = api.interpreter.memoize.flatten

  // TODO convert to string in order to benefit from string diffing in next ZIO release
  private def runTestCase(name: String) = {

    import io.circe.syntax._

    for {
      interpreter             <- memoizedInterpreter
      userDir                 <- system.property("user.dir").someOrFailException.provideLayer(System.live)
      path                     = userDir ++ "/src/test/scala/analytics/api/cases"
      pathFromIntelliJ         = userDir ++ "/analytics.api/src/test/scala/analytics/api/cases"
      fileName                 = name replace (' ', '_')
      query                    = open(s"$path/$fileName.graphql") race open(s"$pathFromIntelliJ/$fileName.graphql")
      expected                 = open(s"$path/$fileName.json") race open(s"$pathFromIntelliJ/$fileName.json")
      (gqlResponse, expected) <- (query >>= (interpreter.execute(_))) zipPar (expected >>= parse)
    } yield assert(gqlResponse.asJson)(equalTo(expected))

  }

  private def parse(str: String) = ZIO fromEither circeParse(str)

  override def spec =
    suite("analytics graphql api")(
      testM("landings and summaries filtered") {
        runTestCase("landings and summaries filtered")
      },
      testM("landings for one month") {
        runTestCase("landings for one month")
      },
      testM("landings for several months including an empty one") {
        runTestCase("landings for several months including an empty one")
      },
      testM("landings summaries for several months including an empty one") {
        runTestCase("landings summaries for several months including an empty one")
      },
      testM("landings summary for one month") {
        runTestCase("landings summary for one month")
      },
    ).provideSomeLayer[ZEnv](db.test)

}
