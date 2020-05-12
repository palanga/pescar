package analytics.api.database.landings

import analytics.api.database.landings.module.LandingsDatabase
import analytics.api.types.Filter
import analytics.api.types.Metric.Landing
import io.file.open
import zio.blocking.Blocking
import zio.stream.ZStream
import zio.{ ZIO, ZLayer }

object Dummy {

  private val loadDataFromJson = {

    import io.circe.generic.auto._
    import io.circe.parser.decode

    // TODO hardcoded string
    open("/Users/palan/code/pescar/analytics.api/src/main/resources/landings-2010-2019-utf8.json")
      .map(decode[List[Landing]])
      .flatMap(ZIO fromEither _)

  }

  val make: ZLayer[Blocking, Throwable, LandingsDatabase] =
    ZLayer fromEffect loadDataFromJson.map(landings => new Dummy(landings))

}

private final class Dummy(landings: List[Landing]) extends LandingsDatabase.Service {

  override def landingsFromFilter(filter: Filter): ZStream[Blocking, Throwable, Landing] =
    ZStream fromIterable landings.filter(landing =>
      (filter.dates contains landing.date) &&
        (filter.locations contains landing.location.name) &&
        (filter.species contains landing.specie.name) &&
        (filter.fleets contains landing.fleet.name)
    )

  override def landingsSummaryFromFilter(filter: Filter): ZIO[Blocking, Throwable, Int] =
    landingsFromFilter(filter).map(_.catchCount).fold(0)(_ + _)

}
