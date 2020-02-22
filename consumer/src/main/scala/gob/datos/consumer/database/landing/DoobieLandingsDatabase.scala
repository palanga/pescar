package gob.datos.consumer.database.landing

import java.time.{ LocalDate, YearMonth }

import doobie.util.Put
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import gob.datos.consumer.database.landing.types.BlockingIO
import gob.datos.consumer.types.Landing
import zio.blocking.Blocking

trait DoobieLandingsDatabase extends LandingsDatabase {

  protected val transactor: Transactor[BlockingIO]

  override val landingsDatabase = new LandingsDatabase.Service[Blocking] {

    override def save(landing: Landing) = ???

    override def saveMany(landings: Iterable[Landing]) = {

      import cats.instances.list.catsStdInstancesForList
      import doobie.syntax.connectionio.toConnectionIOOps
      import instances.yearMonthPut
      import zio.interop.catz.taskConcurrentInstance

      Update[Landing](sql.insertMany)
        .updateMany(landings.toList)
        .transact(transactor)
        .as(landings)

    }

  }

}

object DoobieLandingsDatabase {

  import cats.effect.Blocker
  import config.DBConfig
  import doobie.hikari.HikariTransactor
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

object sql {

  import doobie.implicits._

  val insertMany = sql"INSERT INTO landings VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)".query.sql

}

object instances {
  implicit val yearMonthPut: Put[YearMonth] = Put[LocalDate].contramap(_ atDay 1)
}
