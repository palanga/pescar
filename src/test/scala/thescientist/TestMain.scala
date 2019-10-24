package thescientist

import io.circe.literal._
import thescientist.syntax.gqlquery._
import zio.test.Assertion._
import zio.test._

object TestMain
    extends DefaultRunnableSpec(
      suite("Main")(
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
              |  metrics(title: "langostinos") {
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
                  }
                ]
              }
            }"""

          query.runAsJson map (assert(_, equalTo(expected)))

        },
      )
    )
