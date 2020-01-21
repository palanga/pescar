package gob.datos.consumer.database.landing

import gob.datos.consumer.types.Landing
import zio.RIO
import zio.blocking.Blocking

object types {

  type BlockingIO[A] = RIO[Blocking, A]

  // TODO shit
  case class SomeLandingsNotInserted(landings: Iterable[Landing], updatedCount: Int)
      extends Exception(
        s"""
           |$updatedCount out of ${landings.size} of these landings where inserted:
           |${landings.mkString("\n")}
           |""".stripMargin
      )

  // TODO shit
  case class DatabaseError(landings: Iterable[Landing], throwable: Throwable)
      extends Exception(
        s"""
           |${throwable.getMessage}
           |${landings.mkString("\n")}
           |""".stripMargin
      )

}
