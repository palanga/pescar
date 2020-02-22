package analytics.consumer.gob.datos.database.landing

import zio.RIO
import zio.blocking.Blocking

object types {
  type BlockingIO[A] = RIO[Blocking, A]
}
