package analytics.consumer.gob.datos.database.landing

import java.time.YearMonth

import analytics.consumer.gob.datos.types.Landing
import doobie.util.transactor.Transactor
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.{ Has, RIO, ZIO }

// TODO Blocking should be hidden by LandingsDatabase
object module {

  type BlockingIO[A]      = RIO[Blocking, A]
  type LandingsDatabase   = Has[LandingsDatabase.Service]
  type BlockingTransactor = Has[Transactor[BlockingIO]]

  object LandingsDatabase {
    trait Service {
      def save(landing: Landing): RIO[Blocking, Landing]
      def saveMany(landings: Iterable[Landing]): RIO[Blocking, Iterable[Landing]]
      def find(dates: Set[YearMonth]): ZStream[Blocking, Throwable, Landing]
    }
  }

  def save(landing: Landing): ZIO[LandingsDatabase with Blocking, Throwable, Landing] =
    ZIO.accessM(_.get.save(landing))

  def saveMany(landings: Iterable[Landing]): ZIO[LandingsDatabase with Blocking, Throwable, Iterable[Landing]] =
    ZIO.accessM(_.get.saveMany(landings))

  def find(dates: Set[YearMonth]): ZStream[LandingsDatabase with Blocking, Throwable, Landing] =
    ZStream.accessStream(_.get.find(dates))

  val doobie = DoobieLandingsDatabase.make _

}
