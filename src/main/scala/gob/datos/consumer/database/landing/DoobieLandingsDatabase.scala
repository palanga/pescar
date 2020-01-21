package gob.datos.consumer.database.landing

import doobie.util.transactor.Transactor
import doobie.util.update.Update
import types.{ BlockingIO, DatabaseError, SomeLandingsNotInserted }
import gob.datos.consumer.types.Landing
import zio.ZIO
import zio.blocking.Blocking

trait DoobieLandingsDatabase extends LandingsDatabase {

  protected val transactor: Transactor[BlockingIO]

  override val landingsDatabase = new LandingsDatabase.Service[Blocking] {

    override def save(landing: Landing) = ???

    override def saveMany(landings: Iterable[Landing]) = {

      import cats.implicits._
      import doobie.implicits._
      import zio.interop.catz._

      val sql = "INSERT INTO landings VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

      Update[Landing](sql)
        .updateMany(landings.toList)
        .transact(transactor)
        .mapError(t => DatabaseError(landings, t)) // TODO shit
        .flatMap( // TODO use ZIO.when ?
          updatedCount =>
            if (updatedCount == landings.size) ZIO.succeed(landings)
            else ZIO.fail(SomeLandingsNotInserted(landings, updatedCount)) // TODO shit
        )

    }

  }

}

object DoobieLandingsDatabase {

  import cats.effect.Blocker
  import doobie.hikari.HikariTransactor
  import config.DBConfig
  import zio.ZIO
  import zio.blocking.Blocking
  import zio.interop.catz._

  def makeManaged(config: DBConfig) =
    ZIO.runtime[Blocking].toManaged_ flatMap { implicit runtime =>
      for {
        blockingExecutor <- runtime.environment.blocking.blockingExecutor.toManaged_
        hikariTransactor <- HikariTransactor
                             .newHikariTransactor[BlockingIO](
                               config.driverName,
                               config.url,
                               config.username,
                               config.password,
                               runtime.platform.executor.asEC,
                               Blocker.liftExecutionContext(blockingExecutor.asEC)
                             )
                             .toManaged
      } yield {
        new DoobieLandingsDatabase {
          override protected val transactor: Transactor[BlockingIO] = hikariTransactor
        }
      }
    }

}
