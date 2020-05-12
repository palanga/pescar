package analytics.api.database.landings

import analytics.api.database.landings.module.LandingsDatabase
import analytics.api.types
import analytics.api.types.Metric.Landing
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.{ ZIO, ZLayer }

object Test {
  val make: ZLayer[Any, Nothing, LandingsDatabase] = ZLayer fromFunction (_ => new Test(TestData.LANDINGS_ALL))
}

private final class Test(landings: List[Landing]) extends LandingsDatabase.Service {

  override def landingsFromFilter(filter: types.Filter): ZStream[Blocking, Throwable, Landing] =
    ZStream fromIterable landings

  override def landingsSummaryFromFilter(filter: types.Filter): ZIO[Blocking, Throwable, Int] =
    landingsFromFilter(filter).map(_.catchCount).fold(0)(_ + _)

}
