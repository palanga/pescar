package analytics.consumer.gob.datos.database.landing

import java.time.YearMonth

import analytics.consumer.gob.datos.types.Landing
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.{ RIO, ZIO }

trait LandingsDatabase {
  val landingsDatabase: LandingsDatabase.Service[Blocking]
}

object LandingsDatabase {

  trait Service[R] {
    def save(landing: Landing): RIO[R, Landing]
    def saveMany(landings: Iterable[Landing]): RIO[R, Iterable[Landing]]
    def find(dates: Set[YearMonth]): ZStream[R, Throwable, Landing]
  }

  object module extends Service[LandingsDatabase with Blocking] {
    override def save(landing: Landing)                = ZIO.accessM(_.landingsDatabase.save(landing))
    override def saveMany(landings: Iterable[Landing]) = ZIO.accessM(_.landingsDatabase.saveMany(landings))
    override def find(dates: Set[YearMonth]) =
      ZStream.accessM(_.landingsDatabase.find(dates).mapError(throw _)) // TODO next version of ZStream solves this
  }

}
