package api

import java.time.YearMonth

import api.Main.AppEnv
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.Value.StringValue
import caliban.schema.{ GenericSchema, Schema }

object graphql extends GenericSchema[AppEnv] {

  import java.time.YearMonth
  import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

  import api.types.Metric.Landing
  import api.types._

  implicit val yearMonthSchema: Schema.Typeclass[YearMonth] =
    scalarSchema[YearMonth](
      "YearMonth",
      None,
      yearMonth => StringValue(yearMonth.atDay(1).format(ISO_LOCAL_DATE).dropRight(3))
    )

  case class Queries(
    metrics: List[api.types.Metric],
    landings: landingsResolver.GroupedLandings,
  )

  private val emptyLanding =
    Landing(
      YearMonth.of(2017, 7),
      Fleet(""),
      Location(Port("", None), Department(""), Province("")),
      Specie("", Category(""), CategoryGroup("")),
      7,
    )

  val make =
    graphQL(
      RootResolver(
        Queries(
          emptyLanding :: Nil,
          landingsResolver.all,
        )
      )
    )

  val borrar = typess.Landing.empty.copy(
    byDate = List(
      typess.Landing.empty.copy(
        date = YearMonth.of(2017, 7),
        catchCount = 7,
        byPort = List(
          typess.Landing.empty.copy(
            port = typess.Port("Mar del Plata"),
            catchCount = 5,
          ),
          typess.Landing.empty.copy(
            port = typess.Port("Puerto Madryn"),
            catchCount = 2,
          ),
        ),
      ),
      typess.Landing.empty.copy(
        date = YearMonth.of(2017, 8),
        catchCount = 12,
        byPort = List(
          typess.Landing.empty.copy(
            port = typess.Port("Mar del Plata"),
            catchCount = 7,
          ),
          typess.Landing.empty.copy(
            port = typess.Port("Puerto Madryn"),
            catchCount = 5,
          ),
        ),
      )
    ),
  ) :: Nil

}

object landingsDatabase {

  import typess._

  val MarDelPlata = Port("Mar del Plata")
  val PuertoMadryn = Port("Puerto Madryn")

  case class LandingSummary[A](
    key: A,
    catchCount: Int,
    byDate: List[LandingSummary[YearMonth]] = Nil,
    byPort: List[LandingSummary[Port]] = Nil,
  )

  val all = List(
    Landing(YearMonth.of(2017, 7), MarDelPlata, 5),
    Landing(YearMonth.of(2017, 7), PuertoMadryn, 2),
    Landing(YearMonth.of(2017, 8), MarDelPlata, 17),
    Landing(YearMonth.of(2017, 8), PuertoMadryn, 13),
  )

  def summaryByPortForDate(date: YearMonth) = Map(
    YearMonth.of(2017, 7) -> List(
      LandingSummary(MarDelPlata, 5),
      LandingSummary(PuertoMadryn, 2),
    ),
    YearMonth.of(2017, 8) -> List(
      LandingSummary(MarDelPlata, 17),
      LandingSummary(PuertoMadryn, 13),
    ),
  ).getOrElse(date, Nil)

  def summaryByDateForPort(port: Port) = Map(
    MarDelPlata -> List(
      LandingSummary(YearMonth.of(2017, 7), 5),
      LandingSummary(YearMonth.of(2017, 8), 17),
    ),
    PuertoMadryn -> List(
      LandingSummary(YearMonth.of(2017, 7), 2),
      LandingSummary(YearMonth.of(2017, 8), 13),
    ),
  ).getOrElse(port, Nil)

  val summaryByDate = List(
    LandingSummary(YearMonth.of(2017, 7), 7),
    LandingSummary(YearMonth.of(2017, 8), 30),
  )

  val summaryByPort = List(
    LandingSummary(MarDelPlata, 22),
    LandingSummary(PuertoMadryn, 15),
  )

}

object landingsResolver {

  import typess._
  import landingsDatabase._

  case class GroupedLandings(
    byDate: List[LandingSummary[YearMonth]] = Nil,
    byPort: List[LandingSummary[Port]] = Nil,
  )

  def all = GroupedLandings(
    summaryByDate.map(summary => summary.copy(byPort = summaryByPortForDate(summary.key))),
    summaryByPort.map(summary => summary.copy(byDate = summaryByDateForPort(summary.key))),
  )

  type Filter = Landing => Boolean

}

object typess {

  case class Landing(
    date: java.time.YearMonth,
    port: Port,
    catchCount: Int,
    byDate: List[Landing] = Nil,
    byPort: List[Landing] = Nil,
  )

  case class Port(name: String)

  object Landing {
    val empty = Landing(
      java.time.YearMonth.of(0, 1),
      Port(""),
      0,
      Nil,
      Nil,
    )
  }

}
