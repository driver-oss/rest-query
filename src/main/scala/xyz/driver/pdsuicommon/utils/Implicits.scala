package xyz.driver.pdsuicommon.utils

import scala.collection.generic.CanBuildFrom

object Implicits {

  final class ConditionalAppend[U, T[U] <: TraversableOnce[U]](val c: T[U]) extends AnyVal {
    def condAppend(cond: => Boolean, value: U)(implicit cbf: CanBuildFrom[T[U], U, T[U]]): T[U] = {
      val col = cbf()
      if (cond) {
        ((col ++= c) += value).result
      } else {
        c.asInstanceOf[T[U]]
      }
    }
  }

  implicit def traversableConditionalAppend[U, T[U] <: TraversableOnce[U]](c: T[U]): ConditionalAppend[U, T] =
    new ConditionalAppend[U, T](c)

  implicit def toMapOps[K, V](x: Map[K, V]): MapOps[K, V] = new MapOps(x)

  implicit def toCharOps(self: Char): CharOps       = new CharOps(self)
  implicit def toStringOps(self: String): StringOps = new StringOps(self)
}
