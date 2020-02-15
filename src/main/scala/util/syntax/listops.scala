package util.syntax

object listops {

  implicit class ListOps[A, B, C](val self: List[(A, B, C)]) extends AnyVal {

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

}
