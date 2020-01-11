package gob.datos.consumer.persistence.record

import gob.datos.consumer.types.Record
import zio.RIO
import zio.blocking.Blocking

object types {

  type BlockingIO[A] = RIO[Blocking, A]

  case class SomeRecordsNotInserted(records: Iterable[Record], updatedCount: Int)
      extends Exception(
        s"""
           |$updatedCount out of ${records.size} of these records where inserted:
           |${records.mkString("\n")}
           |""".stripMargin
      )

  case class PersistenceError(records: Iterable[Record], throwable: Throwable)
      extends Exception(
        s"""
           |${throwable.getMessage}
           |${records.mkString("\n")}
           |""".stripMargin
      )

}
