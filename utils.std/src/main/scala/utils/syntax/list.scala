package utils.syntax

import scala.collection.immutable.SortedMap

object list {

  implicit class PairListOps[K, V](val self: List[(K, V)]) extends AnyVal {

    def toSortedMap(implicit ord: Ordering[K]): SortedMap[K, V] = SortedMap from self

  }

  implicit class TriadListOps[A, B, C](val self: List[(A, B, C)]) extends AnyVal {

    /**
     * Convert a list of triads into a Map of Maps.
     * Similar to [[scala.collection.IterableOnceOps.toMap]] but using the first
     * component as the key for the outer Map and the second component as the
     * key for the inner Maps.
     */
    def toBiMap: Map[A, Map[B, C]] =
      self
        .groupMap(_._1)(tuple => (tuple._2, tuple._3))
        .map(kv => kv._1 -> kv._2.toMap)

  }

  implicit class TetradListOps[A, B, C, D](val self: List[(A, B, C, D)]) extends AnyVal {

    /**
     * Similar to [[TriadListOps.toBiMap]] but one step further.
     */
    def toTriMap: Map[A, Map[B, Map[C, D]]] =
      self
        .groupMap(_._1)(tuple => (tuple._2, tuple._3, tuple._4))
        .map(kv => kv._1 -> kv._2.toBiMap)

  }

  implicit class QuintetListOps[A, B, C, D, E](val self: List[(A, B, C, D, E)]) extends AnyVal {

    /**
     * Similar to [[TetradListOps.toTriMap]] but one step further.
     */
    def toTetraMap: Map[A, Map[B, Map[C, Map[D, E]]]] =
      self
        .groupMap(_._1)(tuple => (tuple._2, tuple._3, tuple._4, tuple._5))
        .map(kv => kv._1 -> kv._2.toTriMap)

  }

}
