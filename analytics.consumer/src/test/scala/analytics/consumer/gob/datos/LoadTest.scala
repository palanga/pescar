package analytics.consumer.gob.datos

import analytics.consumer.gob.datos.Main.loadDataFromCsv
import analytics.consumer.gob.datos.database.landing.{
  DoobieLandingsDatabase,
  LandingsDatabase,
  TestDoobieLandingsDatabase
}
import utils.zio.syntax
import zio.ZManaged
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.test.{ assert, suite, testM, DefaultRunnableSpec }

object LoadTest
    extends DefaultRunnableSpec(
      suite("analytics consumer datos.gob load test")(
        testM("load test db") {

          import syntax.ziointerop.iterableops._
          import syntax.zioops._

          val TIMES = 10

          loadDataFromCsv
            .map(
              landings =>
                landings.take(1) :: // warm up
                  landings.take(1) ::
                  landings.take(10) ::
                  landings.take(100) ::
                  landings.take(1000) ::
                  landings.take(10000) ::
                  landings.take(2) ::
                  landings.take(20) ::
                  landings.take(200) ::
                  landings.take(2000) ::
                  landings.take(20000) ::
                  landings.take(4) ::
                  landings.take(40) ::
                  landings.take(400) ::
                  landings.take(4000) ::
                  landings.take(40000) ::
                Nil
            )
            .flatMap { landingsList =>
              TestDoobieLandingsDatabase.deleteTableManaged.use { delete =>
                landingsList.flatMap { landings =>
                  (for {
                    _      <- delete
                    (d, l) <- LandingsDatabase.module.saveMany(landings).timed
                  } yield s"${l.size} landings saved in ${d.render}").times(TIMES)
                }.collectAll
              }
            }
            .map(_.drop(TIMES)) // drop warm up results
            .tapPrint(_.mkString("\n"))
            .tapPrintTimed("Total time: ")
            .as(assert((), zio.test.Assertion.anything))
            .provideSomeManaged(stress_helper.dependencies)
            .orDie

        }
      ) @@ zio.test.TestAspect.ignore
    )

object stress_helper {
  val dependencies =
    for {
      env    <- ZManaged.environment[Blocking]
      doobie <- DoobieLandingsDatabase.makeManaged(config.Config.test.db)
    } yield {
      new Blocking with Clock.Live with Console.Live with LandingsDatabase {
        override val blocking         = env.blocking
        override val landingsDatabase = doobie.landingsDatabase
      }
    }
}
