package time

import scala.util.Try

object YearMonth {
  def parse(string: String) = Try { java.time.YearMonth.parse(string) }
}
