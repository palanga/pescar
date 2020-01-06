package gob.datos.consumer

import doobie.implicits._
import doobie.util.transactor.{ Transactor => DoobieTransactor }
import doobie.util.update.Update
import zio.Task
import zio.interop.catz._

// TODO redo
trait Repository {

  protected def _xa: DoobieTransactor[Task]

//  import RecordSyntax._

  def save(record: types.Record): Task[Int] = sql"INSERT INTO records VALUES (${record.captura}, ${record.categoria}, ${record.departamento}, ${record.departamento_id}, ${record.especie}, ${record.especie_agrupada}, ${record.fecha}, ${record.flota}, ${record.latitud}, ${record.longitud}, ${record.provincia}, ${record.provincia_id}, ${record.puerto})".update.run.transact(_xa)

  def saveMany(elems: List[types.Record]): Task[Int] = {

//    import cats._
    import cats.implicits._

    val sql = "INSERT INTO records VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    Update[types.Record](sql).updateMany(elems).transact(_xa)
  }

}

//object RecordSyntax {
//
//  implicit class RecordOps(record: Types.Record) {
//
//    def toTupleString =
//      s"(${record.captura}, ${record.categoria}, ${record.departamento}, ${record.departamento_id}, ${record.especie}, ${record.especie_agrupada}, ${record.fecha}, ${record.flota}, ${record.latitud}, ${record.longitud}, ${record.provincia}, ${record.provincia_id}, ${record.puerto})"
//
//  }
//
//}
