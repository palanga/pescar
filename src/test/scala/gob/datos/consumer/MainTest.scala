package gob.datos.consumer

import gob.datos.consumer.persistence.record.{ RecordPersistence, TestDoobieDesembarques }
import zio.ZManaged
import zio.blocking.Blocking
import zio.test.Assertion.equalTo
import zio.test.{ DefaultRunnableSpec, suite, testM, assert }

// TODO
object MainTest
    extends DefaultRunnableSpec(
      suite("datos.gob.consumer")(
        testM("delete whole table") {
          TestDoobieDesembarques.deleteTableManaged
            .use(
              _.map(
                assert(
                  _,
                  equalTo(())
                )
              )
            )
        },
        testM("insert some desembarques") {

          val data = List(
            types.Record("2010-01", "Costeros", "Caleta Cordova", "Chubut", 26, "Escalante", 26021, None, None, "Peces", "Merluza hubbsi", "Merluza hubbsi S41", 386114),
            types.Record("2010-01", "Costeros", "Caleta Cordova", "Chubut", 26, "Escalante", 26021, None, None, "Peces", "Pez gallo", "otras especies", 4367),
            types.Record("2010-01", "Costeros", "Caleta Cordova", "Chubut", 26, "Escalante", 26021, None, None, "Peces", "Rayas nep", "Rayas (sin V. Cost)", 13)
          )

          TestDoobieDesembarques.deleteTableManaged.use { delete =>
            for {
              _     <- delete
              saved <- persistence.record.RecordPersistence.module.saveMany(data)
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
      doobie <- persistence.record.DoobieRecordPersistence.makeManaged(config.db)
      //      sttp   <- http.client.SttpClient.makeManaged
    } yield {
      new Blocking /*with Console*/ /*with HttpClient*/ with RecordPersistence {
        override val blocking = env.blocking
//        override val console  = env.console
        override val recordPersistence = doobie.recordPersistence
        //        override val httpClient        = sttp.httpClient
      }
    }

}