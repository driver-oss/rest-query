package xyz.driver.common.logging

/**
  * Use it with care!
  */
case class Unsafe[T](private[logging] val value: T) extends PhiString(Option(value).map(_.toString).getOrElse("<null>"))
