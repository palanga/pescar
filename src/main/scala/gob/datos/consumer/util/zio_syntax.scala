package gob.datos.consumer.util

import zio.ZIO
import zio.stream.ZStream

object zio_syntax {

  implicit final class ZStreamOps[-R, +E, +A](private val self: ZStream[R, E, A]) extends AnyVal {

    // Uncurry in order to get better type inference
    def mapMPar_[R1 <: R, E1 >: E, B](n: Int, f: A => ZIO[R1, E1, B]): ZStream[R1, E1, B] =
      self.mapMPar[R1, E1, B](n)(f)

  }

}
