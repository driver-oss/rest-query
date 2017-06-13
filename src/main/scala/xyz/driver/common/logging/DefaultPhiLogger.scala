package xyz.driver.common.logging

import org.slf4j.{Logger => Underlying}

class DefaultPhiLogger private[logging](underlying: Underlying) extends PhiLogger {

  def error(message: PhiString): Unit = underlying.error(message.text)

  def warn(message: PhiString): Unit = underlying.warn(message.text)

  def info(message: PhiString): Unit = underlying.info(message.text)

  def debug(message: PhiString): Unit = underlying.debug(message.text)

  def trace(message: PhiString): Unit = underlying.trace(message.text)

}
