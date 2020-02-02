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
        testM("group by date and then by port") {

          val query =
            """
              |{
              |	 landings {
              |    byDate {
              |      key
              |      catchCount
              |      byPort {
              |        key {
              |          name
              |        }
              |        catchCount
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "landings": {
                  "byDate": [
                    {
                      "key": "2017-07",
                      "catchCount": 7,
                      "byPort": [
                        {
                          "key": {
                            "name": "Mar del Plata"
                          },
                          "catchCount": 5
                        },
                        {
                          "key": {
                            "name": "Puerto Madryn"
                          },
                          "catchCount": 2
                        }
                      ]
                    },
                    {
                      "key": "2017-08",
                      "catchCount": 30,
                      "byPort": [
                        {
                          "key": {
                            "name": "Mar del Plata"
                          },
                          "catchCount": 17
                        },
                        {
                          "key": {
                            "name": "Puerto Madryn"
                          },
                          "catchCount": 13
                        }
                      ]
                    }
                  ]
                }
              }
            }"""

          query.runOn(Main.httpApp) map (assert(_, equalTo(expected)))

        },
      )
    )
