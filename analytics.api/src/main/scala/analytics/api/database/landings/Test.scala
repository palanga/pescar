package analytics.api.database.landings

import analytics.api.database.landings.module.LandingsDatabase
import analytics.api.types
import analytics.api.types.Metric.Landing
import zio.ZIO
import zio.blocking.Blocking
import zio.stream.ZStream

object Test extends LandingsDatabase.Service {

  override def landingsFromFilter(filter: types.Filter): ZStream[Blocking, Throwable, Landing] =
    ZStream fromIterable TestData.LANDINGS_ALL

  override def landingsSummaryFromFilter(filter: types.Filter): ZIO[Blocking, Throwable, Int] =
    landingsFromFilter(filter).map(_.catchCount).fold(0)(_ + _)

}
