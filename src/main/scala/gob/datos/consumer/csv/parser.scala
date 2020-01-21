package gob.datos.consumer.csv

import gob.datos.consumer.types.Landing

object parser {

  def parseLine = split _ andThen fixIfNecessary andThen parse

  private def split(string: String) = string.split(',')

  /**
   * The csv may have valid text values with commas.
   *
   * line 13975: 2012-05,Fresqueros,otros puertos Buenos Aires,Buenos Aires,6,
   * sin especificar,6999,None,None,otras,Otras - Algas, etc.,otras especies,2717
   * _____________________________________*******************____________________
   *
   */
  private def fixIfNecessary(split: Array[String]) =
    if ((split.length > 11) && (split(11) != " etc.")) split
    else split.updated(10, split(10) + ", etc.").splitAt(11) match { case (left, right) => left ++ right.drop(1) }

  private def parse(split: Array[String]) =
    split.toList match {
      case fecha
            :: flota
            :: puerto
            :: provincia
            :: AsInt(provincia_id)
            :: departamento
            :: AsInt(departamento_id)
            :: latitud
            :: longitud
            :: categoria
            :: especie
            :: especie_agrupada
            :: AsInt(captura)
            :: Nil =>
        Right(
          Landing(
            fecha,
            flota,
            puerto,
            provincia,
            provincia_id,
            departamento,
            departamento_id,
            latitud.toFloatOption,
            longitud.toFloatOption,
            categoria,
            especie,
            especie_agrupada,
            captura,
          )
        )
      case line => Left(LineParseError from line)
    }

  object AsInt {
    def unapply(arg: String): Option[Int] = arg.toIntOption
  }

  case class LineParseError(message: String) extends AnyVal {
    override def toString: String = message
  }

  object LineParseError {

    def from(csvLine: List[String]): LineParseError = csvLine match {
      case _ :: _ :: _ :: _ :: provId :: _ :: depId :: _ :: _ :: _ :: _ :: _ :: cap :: Nil =>
        val columns =
          List(5 -> provId, 7 -> depId, 13 -> cap) // zip with column number
            .map { case (col, value) => (col, value.toIntOption.toRight(value)) } // try parse
            .collect { case (col, Left(value)) => (col, value) } // keep failed only
            .map { case (col, value) => s"""column $col "$value"""" }
            .mkString(", ")
        LineParseError("Couldn't parse " + columns)
      case _ =>
        LineParseError(s"""Couldn't parse "${ csvLine.mkString(",") }"""")
    }

  }

}
