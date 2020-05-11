package analytics.consumer.gob.datos

import analytics.consumer.gob.datos.Main.loadDataFromCsv
import analytics.consumer.gob.datos.database.landing.{ TestDoobieLandingsDatabase, module => db }
import config.Config
import utils.zio.syntax
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.test._

object LoadTest extends DefaultRunnableSpec {
  override def spec =
    suite("analytics consumer datos.gob load test")(
      testM("load test db") {

        import syntax.ziointerop.iterableops._
        import syntax.zioops._

        val TIMES = 10

        loadDataFromCsv
          .map(landings =>
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
                  (d, l) <- db.saveMany(landings).timed
                } yield s"${l.size} landings saved in ${d.render}").times(TIMES)
              }.collectAll
            }
          }
          .map(_.drop(TIMES)) // drop warm up results
          .tapPrint(_.mkString("\n"))
          .tapPrintTimed("Total time: ")
          .as(assert(())(Assertion.anything))
          .provideSomeLayer[Blocking with Console with Clock](db.doobie(Config.test.db)) // TODO

      }
    ) @@ zio.test.TestAspect.ignore
}
