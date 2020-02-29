package analytics.consumer.gob.datos.database.landing

import java.time.{ LocalDate, YearMonth }

import analytics.consumer.gob.datos.database.landing.types.BlockingIO
import analytics.consumer.gob.datos.types.Landing
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import doobie.util.{ Get, Put }
import zio.blocking.Blocking
import zio.stream.ZStream

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

    override def find(dates: Set[YearMonth]) = {

      import doobie.syntax.connectionio.toConnectionIOOps
      import instances.{ yearMonthGet, yearMonthPut }
      import zio.interop.catz.taskConcurrentInstance

      def zioRes = sql.findByDate(dates.toList).query[Landing].stream.compile.toList.transact(transactor)

      if (dates.isEmpty) ZStream.empty else ZStream.fromEffect(zioRes).flatMap(ZStream.fromIterable)

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

  def makeManagedWithBlocking =
    ZIO.runtime[Blocking].toManaged_ flatMap { implicit runtime =>
      for {
        blockingExecutor <- runtime.environment.blocking.blockingExecutor.toManaged_
        hikariTransactor <- HikariTransactor
          .newHikariTransactor[BlockingIO](
            "org.postgresql.Driver",
            "jdbc:postgresql://postgres:5432/datos_gob",
            "palan",
            "",
            runtime.platform.executor.asEC,
            Blocker.liftExecutionContext(blockingExecutor.asEC)
          )
          .toManaged
      } yield {
        new DoobieLandingsDatabase with Blocking {
          override           val blocking  : Blocking.Service[Any]  = runtime.environment.blocking
          override protected val transactor: Transactor[BlockingIO] = hikariTransactor
        }
      }
    }

}

object sql {

  import doobie.implicits._

  val insertMany = sql"INSERT INTO landings VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)".query.sql

  def findByDate(dates: List[YearMonth]) = {
    import cats.implicits._
    import instances.yearMonthPut
    fr"SELECT * FROM landings WHERE fecha " ++ fr"IN (" ++ dates.map(date => fr"$date").intercalate(fr",") ++ fr")"
  }

}

object instances {
  implicit val yearMonthPut: Put[YearMonth] = Put[LocalDate].contramap(_ atDay 1)
  implicit val yearMonthGet: Get[YearMonth] = Get[LocalDate].map(YearMonth.from)
}
