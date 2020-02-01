package api

import io.circe.literal._
import util.syntax.ziointerop.stringops._
import zio.test.Assertion.equalTo
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }

object TestMain
    extends DefaultRunnableSpec(
      suite("graphql api")(
        testM("metric type") {

          val query =
            """
              |{
              |	 metrics {
              |    __typename
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  { "__typename": "Landing" }
                ]
              }
            }"""

          query.runOnV2(Main.httpApp) map (assert(_, equalTo(expected)))

        },
      )
    )
