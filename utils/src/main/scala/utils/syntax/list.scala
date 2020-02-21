package utils.syntax

object list {

  implicit class TriadListOps[A, B, C](val self: List[(A, B, C)]) extends AnyVal {

    /**
     * Convert a list of triads into a Map of Maps.
     * Similar to [[scala.collection.IterableOnceOps.toMap]] but using the first
     * component as the key for the outer Map and the second component as the
     * key for the inner Maps.
     */
    def toMultiMap: Map[A, Map[B, C]] =
      self
        .groupMap(_._1)(tuple => (tuple._2, tuple._3))
        .view
        .mapValues(_.toMap)
        .toMap

  }

  implicit class TetradListOps[A, B, C, D](val self: List[(A, B, C, D)]) extends AnyVal {

    /**
     * Similar to [[TriadListOps.toMultiMap]] but one step further.
     */
    def toMultiMap: Map[A, Map[B, Map[C, D]]] =
      self
        .groupMap(_._1)(tuple => (tuple._2, tuple._3, tuple._4))
        .view
        .mapValues(_.toMultiMap)
        .toMap

  }

  implicit class QuintetListOps[A, B, C, D, E](val self: List[(A, B, C, D, E)]) extends AnyVal {

    /**
     * Similar to [[TetradListOps.toMultiMap]] but one step further.
     */
    def toMultiMap: Map[A, Map[B, Map[C, Map[D, E]]]] =
      self
        .groupMap(_._1)(tuple => (tuple._2, tuple._3, tuple._4, tuple._5))
        .view
        .mapValues(_.toMultiMap)
        .toMap

  }

}
