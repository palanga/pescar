package gob_api_consumer

import doobie.hikari.HikariTransactor
import thescientist.config.DBConfig
import zio.blocking.Blocking
import zio.{ Task, ZIO, ZManaged }

// TODO redo
object Transactor {

  def fromConfig(config: DBConfig): ZManaged[Blocking, Throwable, HikariTransactor[Task]] = {

    import cats.effect.Blocker
    import zio.interop.catz._

    ZIO.runtime[Blocking].toManaged_ >>= { implicit rt =>
      for {
        transactEC <- rt.environment.blocking.blockingExecutor
                       .map(_.asEC)
                       .toManaged_
        connectEC = rt.platform.executor.asEC
        transactor <- HikariTransactor
                       .newHikariTransactor[Task](
                         config.driverName,
                         config.url,
                         config.username,
                         config.password,
                         connectEC,
                         Blocker.liftExecutionContext(transactEC)
                       )
                       .toManaged
      } yield transactor
    }

  }

  val toy: ZManaged[Any, Throwable, doobie.util.transactor.Transactor.Aux[Task, Unit]] = {

    import zio.interop.catz._

    val trans = doobie.util.transactor.Transactor.fromDriverManager[Task](
      "org.postgresql.Driver",
      "jdbc:postgresql://postgres:5432/postgres",
      "postgres",
      "postgres",
    )

    zio.ZIO(trans).toManaged_

  }

}
