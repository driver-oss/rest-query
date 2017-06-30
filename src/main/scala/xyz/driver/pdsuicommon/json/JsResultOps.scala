package xyz.driver.pdsuicommon.json

import play.api.libs.json.JsResult

import scala.util.{Failure, Success, Try}

final class JsResultOps[T](val self: JsResult[T]) extends AnyVal {

  def toTry: Try[T] = {
    self.fold(
      errors => Failure(new JsonValidationException(errors)),
      Success(_)
    )
  }
}
