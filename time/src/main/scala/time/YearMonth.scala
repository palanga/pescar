package time

import java.time.{ YearMonth => JYearMonth }

import scala.util.Try

object YearMonth {
  def parse(string: String) = Try(JYearMonth.parse(string))
}

object AsYearMonth {
  import time.syntax.StringOps
  def unapply(arg: String) = arg.toYearMonthOption
}
