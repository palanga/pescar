package gob.datos.consumer.persistence.record

import gob.datos.consumer.persistence.record.types.BlockingIO

object TestDoobieDesembarques {

  import cats.effect.Blocker
  import doobie.hikari.HikariTransactor
  import zio.ZIO
  import zio.blocking.Blocking
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
            Blocker.liftExecutionContext(blockingExecutor.asEC)
          )
          .toManaged
      } yield {

        import doobie.implicits._
        import zio.interop.catz._

        sql"DELETE FROM desembarques".update.run
          .transact(hikariTransactor)
          .unit

      }
    }

}
