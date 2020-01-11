package gob.datos.consumer.persistence.record

import doobie.util.transactor.Transactor
import doobie.util.update.Update
import gob.datos.consumer.persistence.record.types.{ BlockingIO, PersistenceError, SomeRecordsNotInserted }
import gob.datos.consumer.types.Record
import zio.ZIO
import zio.blocking.Blocking

trait DoobieRecordPersistence extends RecordPersistence {

  protected val transactor: Transactor[BlockingIO]

  override val recordPersistence = new RecordPersistence.Service[Blocking] {

    override def save(record: Record) = ???

    override def saveMany(records: Iterable[Record]) = {

      import cats.implicits._
      import doobie.implicits._
      import zio.interop.catz._

      val sql = "INSERT INTO records VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

      Update[Record](sql)
        .updateMany(records.toList)
        .transact(transactor)
        .mapError(t => PersistenceError(records, t))
        .flatMap(
          updatedCount =>
            if (updatedCount == records.size) ZIO.succeed(records)
            else ZIO.fail(SomeRecordsNotInserted(records, updatedCount))
        )

    }

    override def deleteRecords = {

      import doobie.implicits._
      import zio.interop.catz._

      sql"DELETE FROM records".update.run
        .transact(transactor)
        .unit

    }

  }

}

object DoobieRecordPersistence {

  import cats.effect.Blocker
  import doobie.hikari.HikariTransactor
  import thescientist.config.DBConfig
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
        new DoobieRecordPersistence {
          override protected val transactor: Transactor[BlockingIO] = hikariTransactor
        }
      }
    }

}
