package xyz.driver.pdsuicommon

import pureconfig._

import scala.util.{Failure, Success, Try}

object Config {

  implicit def productHint[T]: ProductHint[T] = ProductHint(ConfigFieldMapping(CamelCase, CamelCase))

  def loadConfig[Config](implicit reader: ConfigReader[Config]): Try[Config] = pureconfig.loadConfig match {
    case Right(x) => Success(x)
    case Left(e)  => Failure(new RuntimeException(e.toString))
  }

  def loadConfig[Config](namespace: String)(implicit reader: ConfigReader[Config]): Try[Config] =
    pureconfig.loadConfig(namespace) match {
      case Right(x) => Success(x)
      case Left(e)  => Failure(new RuntimeException(e.toString))
    }
}
