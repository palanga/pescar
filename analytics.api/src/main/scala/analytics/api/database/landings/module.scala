package analytics.api.database.landings

import analytics.api.types.{ Filter, Metric }
import doobie.util.transactor.Transactor
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.{ Has, RIO, ZIO }

object module {

  type BlockingIO[A]      = RIO[Blocking, A]
  type LandingsDatabase   = Has[LandingsDatabase.Service]
  type BlockingTransactor = Has[Transactor[BlockingIO]]

  object LandingsDatabase {
    trait Service {
      def landingsFromFilter(filter: Filter): ZStream[Blocking, Throwable, Metric.Landing]
      def landingsSummaryFromFilter(filter: Filter): ZIO[Blocking, Throwable, Int]
    }
  }

  def landingsFromFilter(filter: Filter): ZStream[LandingsDatabase with Blocking, Throwable, Metric.Landing] =
    ZStream.accessStream(_.get.landingsFromFilter(filter))

  def landingsSummaryFromFilter(filter: Filter): ZIO[LandingsDatabase with Blocking, Throwable, Int] =
    ZIO.accessM(_.get.landingsSummaryFromFilter(filter))

  val inMemory = InMemory.make
  val test     = Test.make

}
