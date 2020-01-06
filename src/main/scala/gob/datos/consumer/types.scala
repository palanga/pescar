package gob.datos.consumer

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
    records: List[Record],
    total: Int,
  )

  case class Links(next: URL, start: URL)

  type URL = String

  // TODO reorder fields
  case class Record(
    captura: Int,
    categoria: String,
    departamento: String,
    departamento_id: Int,
    especie: String,
    especie_agrupada: String,
    fecha: String,
    flota: String,
    latitud: Option[Degree],
    longitud: Option[Degree],
    provincia: String,
    provincia_id: Int,
    puerto: String,
  )

  type Degree = Float

}
