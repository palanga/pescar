package gob.datos.consumer.database.landing

import gob.datos.consumer.types.Landing
import zio.{ RIO, ZIO }
import zio.blocking.Blocking

trait LandingsDatabase {
  val landingsDatabase: LandingsDatabase.Service[Blocking]
}

object LandingsDatabase {

  trait Service[R] {
    def save(landing: Landing): RIO[R, Landing]
    def saveMany(landings: Iterable[Landing]): RIO[R, Iterable[Landing]]
  }

  object module extends Service[LandingsDatabase with Blocking] {
    override def save(landing: Landing)                = ZIO.accessM(_.landingsDatabase.save(landing))
    override def saveMany(landings: Iterable[Landing]) = ZIO.accessM(_.landingsDatabase.saveMany(landings))
  }

}
