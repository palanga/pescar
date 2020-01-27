package gob.datos.consumer

import gob.datos.consumer.database.landing.{ DoobieLandingsDatabase, LandingsDatabase, TestDoobieLandingsDatabase }
import zio.ZManaged
import zio.blocking.Blocking
import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, assert, suite, testM }

// TODO
object MainTest
    extends DefaultRunnableSpec(
      suite("datos.gob.consumer")(
        testM("delete whole table") {
          TestDoobieLandingsDatabase.deleteTableManaged
            .use(
              _.map(
                assert(
                  _,
                  equalTo(())
                )
              )
            )
        },
        testM("insert some landings") {

          val data = List(
            types.Landing(java.time.YearMonth.parse("2010-01"), "Costeros", "Caleta Cordova", "Chubut", 26, "Escalante", 26021, None, None, "Peces", "Merluza hubbsi", "Merluza hubbsi S41", 386114),
            types.Landing(java.time.YearMonth.parse("2010-01"), "Costeros", "Caleta Cordova", "Chubut", 26, "Escalante", 26021, None, None, "Peces", "Pez gallo", "otras especies", 4367),
            types.Landing(java.time.YearMonth.parse("2010-01"), "Costeros", "Caleta Cordova", "Chubut", 26, "Escalante", 26021, None, None, "Peces", "Rayas nep", "Rayas (sin V. Cost)", 13)
          )

          TestDoobieLandingsDatabase.deleteTableManaged.use { delete =>
            for {
              _     <- delete
              saved <- LandingsDatabase.module.saveMany(data)
            } yield assert(saved, equalTo(data))

          }

        } @@ zio.test.TestAspect.ignore,
      ).provideSomeManaged(helper.dependencies.orDie)
    )

// TODO
object helper {

  val dependencies =
    for {
      env <- ZManaged.environment[Blocking]
      config <- config.ConfigLoader.test.toManaged_
      doobie <- DoobieLandingsDatabase.makeManaged(config.db)
      //      sttp   <- http.client.SttpClient.makeManaged
    } yield {
      new Blocking /*with Console*/ /*with HttpClient*/ with LandingsDatabase {
        override val blocking         = env.blocking
//        override val console  = env.console
        override val landingsDatabase = doobie.landingsDatabase
        //        override val httpClient        = sttp.httpClient
      }
    }

}