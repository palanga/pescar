package api

import io.circe.literal._
import util.syntax.ziointerop.stringops._
import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, TestAspect, assert, suite, testM }

object TestMain
  extends DefaultRunnableSpec(
    suite("graphql api")(
      testM("group by date and then by location and then by specie") {

        val query =
          """
            |{
            |	 landings {
            |    byDate {
            |      key
            |      value {
            |        total
            |        byLocation {
            |          key
            |          value {
            |            total
            |            bySpecie {
            |              key
            |              value {
            |                total
            |              }
            |            }
            |          }
            |        }
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
                      "value": {
                        "total": 7,
                        "byLocation": [
                          {
                            "key": "Mar del Plata",
                            "value": {
                              "total": 5,
                              "bySpecie": [
                                {
                                  "key": "Langostino",
                                  "value": { "total": 2 }
                                },
                                {
                                  "key": "Pulpo",
                                  "value": { "total": 3}
                                }
                              ]
                            }
                          },
                          {
                            "key": "Puerto Madryn",
                            "value": {
                              "total": 2,
                              "bySpecie": [
                                {
                                  "key": "Langostino",
                                  "value": { "total": 1 }
                                },
                                {
                                  "key": "Pulpo",
                                  "value": { "total": 1 }
                                }
                              ]
                            }
                          }
                        ]
                      }
                    },
                    {
                      "key": "2017-08",
                      "value": {
                        "total": 30,
                        "byLocation": [
                          {
                            "key": "Mar del Plata",
                            "value": {
                              "total": 17,
                              "bySpecie": [
                                {
                                  "key": "Langostino",
                                  "value": { "total": 10 }
                                },
                                {
                                  "key": "Pulpo",
                                  "value": { "total": 7 }
                                }
                              ]
                            }
                          },
                          {
                            "key": "Puerto Madryn",
                            "value": {
                              "total": 13,
                              "bySpecie": [
                                {
                                  "key": "Langostino",
                                  "value": { "total": 9 }
                                },
                                {
                                  "key": "Pulpo",
                                  "value": { "total": 4 }
                                }
                              ]
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
              }
            }"""

        query.runOn(Main.httpApp) map (assert(_, equalTo(expected)))

      } @@ TestAspect.ignore,
    )
  )
