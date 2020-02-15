package util.syntax

object tupleops {

  implicit class TupleOps[A, B, C](val self: ((A, B), C)) extends AnyVal {
    def flatten: (A, B, C) = self match {
      case ((a, b), c) => (a, b, c)
    }
  }

}
