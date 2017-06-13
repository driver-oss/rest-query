package xyz.driver.pdsuicommon.utils

final class MapOps[K, V](val self: Map[K, V]) extends AnyVal {

  def swap: Map[V, Set[K]] = {
    self.toList
      .groupBy { case (_, v)         => v }
      .mapValues(_.map { case (k, _) => k }.toSet)
  }
}
