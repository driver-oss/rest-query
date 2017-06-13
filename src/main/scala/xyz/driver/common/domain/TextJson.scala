package xyz.driver.common.domain

import xyz.driver.common.logging._

case class TextJson[+T](content: T) {
  def map[U](f: T => U): TextJson[U] = copy(f(content))
}

object TextJson {

  implicit def toPhiString[T](x: TextJson[T])(implicit inner: T => PhiString): PhiString = {
    phi"TextJson(${x.content})"
  }
}
