package api

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
          emptyLanding :: Nil
        )
      )
    )

}
