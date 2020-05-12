package analytics.consumer.gob.datos

import java.time.YearMonth

import analytics.consumer.gob.datos.database.landing.{TestDoobieLandingsDatabase, module => db}
import config.Config
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

// TODO esto es una poronga todo
object DatabaseTest extends DefaultRunnableSpec {

  val deleteTable = TestDoobieLandingsDatabase.deleteTableManaged

  // TODO después del refactor no probé nada, todo esto me da paja
  override def spec =
    suite("analytics consumer datos.gob")(
      testM("delete whole table") {
        deleteTable.use_(ZIO.unit).as(assertCompletes)
      },
      testM("insert some landings") {

        val data = List(
          types
            .Landing(
              YearMonth parse "2010-01",
              "Costeros",
              "Caleta Cordova",
              "Chubut",
              26,
              "Escalante",
              26021,
              None,
              None,
              "Peces",
              "Merluza hubbsi",
              "Merluza hubbsi S41",
              386114,
            ),
          types
            .Landing(
              YearMonth parse "2010-01",
              "Costeros",
              "Caleta Cordova",
              "Chubut",
              26,
              "Escalante",
              26021,
              None,
              None,
              "Peces",
              "Pez gallo",
              "otras especies",
              4367,
            ),
          types
            .Landing(
              YearMonth parse "2010-01",
              "Costeros",
              "Caleta Cordova",
              "Chubut",
              26,
              "Escalante",
              26021,
              None,
              None,
              "Peces",
              "Rayas nep",
              "Rayas (sin V. Cost)",
              13,
            ),
        )

        // TODO ?
        deleteTable.use { delete =>
          for {
            _     <- delete
            saved <- db.saveMany(data).provideSomeLayer(db.doobie(Config.test.db))
          } yield assert(saved)(equalTo(data))

        }

      },
    ) @@ TestAspect.ignore

}
