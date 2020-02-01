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

          query.runOn(Main.httpApp) map (assert(_, equalTo(expected)))

        },
        testM("date and fishCatch") {

          val query =
            """
              |{
              |	 metrics {
              |  __typename
              |  ... on Landing {
              |      date
              |      fishCatch
              |    }
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  {
                    "__typename" : "Landing",
                    "date": "2017-07",
                    "fishCatch": 7
                  }
                ]
              }
            }"""

          query.runOn(Main.httpApp) map (assert(_, equalTo(expected)))

        },
      )
    )
