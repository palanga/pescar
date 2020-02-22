package gob.datos.consumer.database.landing

import zio.RIO
import zio.blocking.Blocking

object types {
  type BlockingIO[A] = RIO[Blocking, A]
}
