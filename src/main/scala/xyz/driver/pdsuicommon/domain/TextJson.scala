package xyz.driver.pdsuicommon.domain

import xyz.driver.pdsuicommon.logging._

final case class TextJson[+T](content: T) {
  def map[U](f: T => U): TextJson[U] = copy(f(content))
}

object TextJson {

  implicit def toPhiString[T](x: TextJson[T])(implicit inner: T => PhiString): PhiString = {
    phi"TextJson(${x.content})"
  }
}
