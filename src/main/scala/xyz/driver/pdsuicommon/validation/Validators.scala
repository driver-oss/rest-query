package xyz.driver.pdsuicommon.validation

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.JsonSerializer

import scala.util.control.NonFatal

object Validators extends PhiLogging {

  type Validator[Input, Refined] = Input => Either[ValidationError, Refined]

  def generic[T, R](message: String)(f: PartialFunction[T, R]): Validator[T, R] = { value =>
    if (f.isDefinedAt(value)) Right(f(value))
    else Left(ValidationError(message))
  }

  def nonEmpty[T](field: String): Validator[Option[T], T] = generic(s"$field is empty") {
    case Some(x) => x
  }

  def nonEmptyString(field: String): Validator[String, String] = generic(s"$field is empty") {
    case x if x.nonEmpty => x
  }

  def deserializableTo[Refined](field: String)(value: String)(
          implicit m: Manifest[Refined]): Either[ValidationError, Refined] = {
    try {
      Right(JsonSerializer.deserialize[Refined](value))
    } catch {
      case NonFatal(e) =>
        logger.error(phi"Can not deserialize the ${Unsafe(field)}: $e")
        Left(ValidationError(s"$field is invalid"))
    }
  }

  def success[T](result: T): Either[Nothing, T] = Right(result)

  def fail(message: String): Either[ValidationError, Nothing] = Left(ValidationError(message))
}
