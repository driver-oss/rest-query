package xyz.driver.pdsuicommon.domain

import java.util.UUID

import xyz.driver.pdsuicommon.logging._

sealed trait Id[+T]

case class CompoundId[Id1 <: Id[_], Id2 <: Id[_]](part1: Id1, part2: Id2) extends Id[(Id1, Id2)]

case class LongId[+T](id: Long) extends Id[T] {
  override def toString: String = id.toString

  def is(longId: Long): Boolean = {
    id == longId
  }
}

object LongId {
  implicit def toPhiString[T](x: LongId[T]): PhiString = Unsafe(s"LongId(${x.id})")
}

case class StringId[+T](id: String) extends Id[T] {
  override def toString: String = id

  def is(stringId: String): Boolean = {
    id == stringId
  }
}

object StringId {
  implicit def toPhiString[T](x: StringId[T]): PhiString = Unsafe(s"StringId(${x.id})")
}

case class UuidId[+T](id: UUID) extends Id[T] {
  override def toString: String = id.toString
}

object UuidId {

  /**
    * @note May fail, if `string` is invalid UUID.
    */
  def apply[T](string: String): UuidId[T] = new UuidId[T](UUID.fromString(string))

  def apply[T](): UuidId[T] = new UuidId[T](UUID.randomUUID())

  implicit def ordering[T] = Ordering.by[UuidId[T], String](_.toString)

  implicit def toPhiString[T](x: UuidId[T]): PhiString = Unsafe(s"UuidId(${x.id})")
}
