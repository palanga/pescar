package analytics.consumer.gob.datos.database.landing

import analytics.consumer.gob.datos.database.landing.module.BlockingIO
import cats.effect.Blocker
import doobie.hikari.HikariTransactor
import zio.ZIO
import zio.blocking.Blocking

object TestDoobieLandingsDatabase {

  import doobie.implicits._
  import zio.interop.catz._

  val deleteTableManaged =
    ZIO.runtime[Blocking].toManaged_ flatMap { implicit runtime =>
      HikariTransactor
        .newHikariTransactor[BlockingIO](
          "org.postgresql.Driver",
          "jdbc:postgresql://postgres:5432/test_datos_gob",
          "palan",
          "",
          runtime.platform.executor.asEC,
          Blocker.liftExecutionContext(runtime.environment.get.blockingExecutor.asEC)
        )
        .toManaged
        .map { transactor =>
          sql"DELETE FROM landings".update.run
            .transact(transactor)
            .unit
        }
    }

}
