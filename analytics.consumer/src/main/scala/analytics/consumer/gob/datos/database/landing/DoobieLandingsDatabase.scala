package analytics.consumer.gob.datos.database.landing

import java.time.{ LocalDate, YearMonth }

import analytics.consumer.gob.datos.database.landing.module.{ BlockingIO, LandingsDatabase }
import analytics.consumer.gob.datos.types.Landing
import cats.effect.Blocker
import config.DBConfig
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import doobie.util.{ Get, Put }
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.{ ZIO, ZLayer }

object DoobieLandingsDatabase {

  import zio.interop.catz._

  // TODO make config a dependency
  def make(config: DBConfig): ZLayer[Blocking, Throwable, LandingsDatabase] =
    ZIO
      .runtime[Blocking]
      .toManaged_
      .flatMap { implicit runtime =>
        HikariTransactor
          .newHikariTransactor[BlockingIO](
            config.driverName,
            config.url,
            config.username,
            config.password,
            runtime.platform.executor.asEC,
            Blocker.liftExecutionContext(runtime.environment.get.blockingExecutor.asEC),
          )
          .toManaged
      }
      .map(new DoobieLandingsDatabase(_))
      .toLayer

}

private final class DoobieLandingsDatabase(transactor: Transactor[BlockingIO]) extends module.LandingsDatabase.Service {

  // TODO
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

  override def find(dates: Set[YearMonth]) = {

    import doobie.syntax.connectionio.toConnectionIOOps
    import instances.yearMonthGet
    import zio.interop.catz.taskConcurrentInstance

    def zioRes = sql.findByDate(dates.toList).query[Landing].stream.compile.toList.transact(transactor)

    if (dates.isEmpty) ZStream.empty else ZStream.fromEffect(zioRes).flatMap(ZStream fromIterable _)

  }

}

private object sql {

  import doobie.implicits._

  val insertMany = sql"INSERT INTO landings VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)".query.sql

  def findByDate(dates: List[YearMonth]) = {
    import cats.implicits._
    import instances.yearMonthPut
    fr"SELECT * FROM landings WHERE fecha " ++ fr"IN (" ++ dates.map(date => fr"$date").intercalate(fr",") ++ fr")"
  }

}

private object instances {
  implicit val yearMonthPut: Put[YearMonth] = Put[LocalDate].contramap(_ atDay 1)
  implicit val yearMonthGet: Get[YearMonth] = Get[LocalDate].map(YearMonth.from)
}
