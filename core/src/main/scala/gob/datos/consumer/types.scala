package gob.datos.consumer

import java.time.YearMonth

object types {

  case class RequestBody(
    limit: Int,
    offset: Int,
    resource_id: ResourceId,
  )

  type ResourceId = String

  case class ResponseBody(result: Result, success: Boolean)

  case class Result(
    resource_id: ResourceId,
    _links: Links,
    limit: Int,
    offset: Int,
    records: List[Landing],
    total: Int,
  )

  case class Links(next: URL, start: URL)

  type URL = String

  case class Landing(
    fecha: YearMonth,
    flota: String,
    puerto: String,
    provincia: String,
    provincia_id: Int,
    departamento: String,
    departamento_id: Int,
    latitud: Option[Degree],
    longitud: Option[Degree],
    categoria: String,
    especie: String,
    especie_agrupada: String,
    captura: Int,
  )

  type Degree = Float

}
