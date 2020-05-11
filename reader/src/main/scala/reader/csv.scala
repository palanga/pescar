package reader

object csv {

  type UnsplittedCsvLine = String

  /**
   * Given a list of comma separated values and a parser function,
   * return either a list of indexed parse failures or a list of success values if no errors were found.
   */
  def parse[E, B](unparsedCsvLines: List[String], parser: UnsplittedCsvLine => Either[E, B]) =
    unparsedCsvLines
      .map(parser)
      .zipWithIndex
      .partitionMap { case (either, index) => either.left.map(Indexed(index + 1, _)) } match {
      case (Nil, value)  => Right(value)
      case (failures, _) => Left(failures)
    }

  case class Indexed[A](index: Int, value: A) {
    override def toString: String = s"$index: $value"
  }

}
