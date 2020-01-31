package api

import io.circe.literal._
import api.metrics.MetricsMock
import api.syntax.gqlquery._
import zio.ZManaged
import zio.clock.Clock
import zio.test.Assertion.equalTo
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }

object TestMain
    extends DefaultRunnableSpec(
      suite("metrics.graphql")(
        testM("Query title") {

          val query =
            """
              |{
              |	 metrics {
              |    title
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  { "title": "Desembarques de langostinos por mes" },
                  { "title": "Desembarques de merluza por mes" },
                  { "title": "Desembarques de pejerrey por mes" },
                  { "title": "Desembarques de pulpo por mes" }
                ]
              }
            }"""

          query.runAsJson map (assert(_, equalTo(expected)))

        },
        testM("Query data -> __typename") {

          val query =
            """
              |{
              |  metrics {
              |    data {
              |      __typename
              |    }
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  { "data": { "__typename": "Histogram" } },
                  { "data": { "__typename": "Histogram" } },
                  { "data": { "__typename": "Histogram" } },
                  { "data": { "__typename": "Histogram" } }
                ]
              }
            }"""

          query.runAsJson map (assert(_, equalTo(expected)))

        },
        testM("Query data -> Histogram") {

          val query =
            """
              |{
              |  metrics {
              |    data {
              |      ... on Histogram {
              |        values {
              |          month
              |          value
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  {
                    "data": {
                      "values": [
                        { "month": "January", "value": 190 },
                        { "month": "February", "value": 23 },
                        { "month": "March", "value": 347 },
                        { "month": "April", "value": 1234 }
                      ]
                    }
                  },
                  {
                    "data": {
                      "values": [
                        { "month": "January", "value": 234 },
                        { "month": "February", "value": 12 },
                        { "month": "March", "value": 234 },
                        { "month": "April", "value": 3267 }
                      ]
                    }
                  },
                  {
                    "data": {
                      "values": [
                        { "month": "January", "value": 345 },
                        { "month": "February", "value": 65 },
                        { "month": "March", "value": 846 },
                        { "month": "April", "value": 2378 }
                      ]
                    }
                  },
                  {
                    "data": {
                      "values": [
                        { "month": "January", "value": 934 },
                        { "month": "February", "value": 92 },
                        { "month": "March", "value": 234 },
                        { "month": "April", "value": 599 }
                      ]
                    }
                  }
                ]
              }
            }"""

          query.runAsJson map (assert(_, equalTo(expected)))

        },
        testM("Query data -> KPI") {

          val query =
            """
              |{
              |  metrics {
              |    data {
              |      ... on KPI {
              |        value
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  { "data": {} },
                  { "data": {} },
                  { "data": {} },
                  { "data": {} }
                ]
              }
            }"""

          query.runAsJson map (assert(_, equalTo(expected)))

        },
        testM("Query title and data") {

          val query =
            """
              |{
              |  metrics {
              |    title
              |    data {
              |      __typename
              |      ... on Histogram {
              |         values {
              |           month
              |           value
              |         }
              |       }
              |      ... on KPI {
              |        value
              |      }
              |    }
              |
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  {
                    "title": "Desembarques de langostinos por mes",
                    "data": {
                      "__typename": "Histogram",
                      "values": [
                        { "month": "January", "value": 190 },
                        { "month": "February", "value": 23 },
                        { "month": "March", "value": 347 },
                        { "month": "April", "value": 1234 }
                      ]
                    }
                  },
                  {
                    "title": "Desembarques de merluza por mes",
                    "data": {
                      "__typename": "Histogram",
                      "values": [
                        { "month": "January", "value": 234 },
                        { "month": "February", "value": 12 },
                        { "month": "March", "value": 234 },
                        { "month": "April", "value": 3267 }
                      ]
                    }
                  },
                  {
                    "title": "Desembarques de pejerrey por mes",
                    "data": {
                      "__typename": "Histogram",
                      "values": [
                        { "month": "January", "value": 345 },
                        { "month": "February", "value": 65 },
                        { "month": "March", "value": 846 },
                        { "month": "April", "value": 2378 }
                      ]
                    }
                  },
                  {
                    "title": "Desembarques de pulpo por mes",
                    "data": {
                      "__typename": "Histogram",
                      "values": [
                        { "month": "January", "value": 934 },
                        { "month": "February", "value": 92 },
                        { "month": "March", "value": 234 },
                        { "month": "April", "value": 599 }
                      ]
                    }
                  }
                ]
              }
            }"""

          query.runAsJson map (assert(_, equalTo(expected)))

        },
        testM("Query filtering by title") {

          val query =
            """
              |{
              |  metrics(keywords: "langostinos") {
              |    title
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  { "title": "Desembarques de langostinos por mes" }
                ]
              }
            }"""

          query.runAsJson map (assert(_, equalTo(expected)))

        },
        testM("Query filtering by title, multiple words") {

          val query =
            """
              |{
              |  metrics(keywords: ["langostinos", "pejerrey"]) {
              |    title
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  { "title": "Desembarques de langostinos por mes" },
                  { "title": "Desembarques de pejerrey por mes" }
                ]
              }
            }"""

          query.runAsJson map (assert(_, equalTo(expected)))

        },
        testM("Query filtering by title by an empty string should return all") {

          val query =
            """
              |{
              |  metrics(keywords: "") {
              |    title
              |  }
              |}
              |""".stripMargin

          val expected =
            json"""{
              "data": {
                "metrics": [
                  { "title": "Desembarques de langostinos por mes" },
                  { "title": "Desembarques de merluza por mes" },
                  { "title": "Desembarques de pejerrey por mes" },
                  { "title": "Desembarques de pulpo por mes" }
                ]
              }
            }"""

          query.runAsJson map (assert(_, equalTo(expected)))

        },
      ).provideSomeManaged(helper.dependencies)
    )

object helper {

  val dependencies =
    ZManaged
      .environment[Clock]
      .map(
        env =>
          new MetricsMock with Main.BaseEnv {
            override val clock = env.clock
        }
      )

}
