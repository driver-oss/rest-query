package xyz.driver.pdsuicommon.logging

trait PhiLogger {

  def error(message: PhiString): Unit

  def warn(message: PhiString): Unit

  def info(message: PhiString): Unit

  def debug(message: PhiString): Unit

  def trace(message: PhiString): Unit

}
