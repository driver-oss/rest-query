package xyz.driver.pdsuicommon.serialization

trait Marshaller[T, Repr] {
  def read(x: Repr): Option[T]
  def write(x: T): Repr
}
