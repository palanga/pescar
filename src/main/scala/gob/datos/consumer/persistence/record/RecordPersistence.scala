package gob.datos.consumer.persistence.record

import gob.datos.consumer.types.Record
import zio.{ RIO, ZIO }
import zio.blocking.Blocking

trait RecordPersistence {
  val recordPersistence: RecordPersistence.Service[Blocking]
}

object RecordPersistence {

  trait Service[R] {
    def save(record: Record): RIO[R, Record]
    def saveMany(records: Iterable[Record]): RIO[R, Iterable[Record]]
    // TODO test only
    def deleteRecords: RIO[R, Unit]
  }

  object module extends Service[RecordPersistence with Blocking] {
    override def save(record: Record)                = ZIO.accessM(_.recordPersistence.save(record))
    override def saveMany(records: Iterable[Record]) = ZIO.accessM(_.recordPersistence.saveMany(records))
    override def deleteRecords                       = ZIO.accessM(_.recordPersistence.deleteRecords)
  }

}
