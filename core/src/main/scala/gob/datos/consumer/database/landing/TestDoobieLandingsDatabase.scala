package gob.datos.consumer.database.landing

import doobie.hikari.HikariTransactor
import gob.datos.consumer.database.landing.types.BlockingIO
import zio.ZIO
import zio.blocking.Blocking

object TestDoobieLandingsDatabase {

  import doobie.implicits._
  import zio.interop.catz._

  val deleteTableManaged =
    ZIO.runtime[Blocking].toManaged_ flatMap { implicit runtime =>
      for {
        blockingExecutor <- runtime.environment.blocking.blockingExecutor.toManaged_
        hikariTransactor <- HikariTransactor
                             .newHikariTransactor[BlockingIO](
                               "org.postgresql.Driver",
                               "jdbc:postgresql://postgres:5432/test_datos_gob",
                               "palan",
                               "",
                               runtime.platform.executor.asEC,
                               cats.effect.Blocker.liftExecutionContext(blockingExecutor.asEC)
                             )
                             .toManaged
      } yield
        sql"DELETE FROM landings".update.run
          .transact(hikariTransactor)
          .unit

    }

}
