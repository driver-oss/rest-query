package xyz.driver.pdsuicommon.error

import xyz.driver.pdsuicommon.json.Serialization.seqJsonFormat
import ErrorCode.{ErrorCode, Unspecified}
import ErrorsResponse.ResponseError
import xyz.driver.pdsuicommon.auth.{AnonymousRequestContext, RequestId}
import xyz.driver.pdsuicommon.utils.Utils
import play.api.http.Writeable
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{Result, Results}
import xyz.driver.pdsuicommon.validation.JsonValidationErrors

class ErrorsResponse(val errors: Seq[ResponseError], private val httpStatus: Results#Status, val requestId: RequestId) {
  def toResult(implicit writeable: Writeable[ErrorsResponse]): Result = httpStatus(this)
}

object ErrorsResponse {

  /**
    * @param data      Any data that can be associated with particular error.Ex.: error field name
    * @param message   Error message
    * @param code      Unique error code
    *
    * @see https://driverinc.atlassian.net/wiki/display/RA/REST+API+Specification#RESTAPISpecification-HTTPStatuscodes
    */
  final case class ResponseError(data: Option[String], message: String, code: ErrorCode)

  object ResponseError {

    implicit val responseErrorJsonFormat: Format[ResponseError] = (
      (JsPath \ "data").formatNullable[String] and
        (JsPath \ "message").format[String] and
        (JsPath \ "code").format[ErrorCode]
    )(ResponseError.apply, unlift(ResponseError.unapply))

  }

  implicit val writes: Writes[ErrorsResponse] = Writes { errorsResponse =>
    Json.obj(
      "errors"    -> Json.toJson(errorsResponse.errors),
      "requestId" -> Json.toJson(errorsResponse.requestId.value)
    )
  }

  // deprecated, will be removed in REP-436
  def fromString(message: String, httpStatus: Results#Status)(
          implicit context: AnonymousRequestContext): ErrorsResponse = {
    new ErrorsResponse(
      errors = Seq(
        ResponseError(
          data = None,
          message = message,
          code = Unspecified
        )),
      httpStatus = httpStatus,
      requestId = context.requestId
    )
  }

  // scalastyle:off null
  def fromExceptionMessage(e: Throwable, httpStatus: Results#Status = Results.InternalServerError)(
          implicit context: AnonymousRequestContext): ErrorsResponse = {
    val message = if (e.getMessage == null || e.getMessage.isEmpty) {
      Utils.getClassSimpleName(e.getClass)
    } else {
      e.getMessage
    }

    fromString(message, httpStatus)
  }
  // scalastyle:on null

  // deprecated, will be removed in REP-436
  def fromJsonValidationErrors(validationErrors: JsonValidationErrors)(
          implicit context: AnonymousRequestContext): ErrorsResponse = {
    val errors = validationErrors.map {
      case (path, xs) =>
        ResponseError(
          data = Some(path.toString()),
          message = xs.map(_.message).mkString("\n"),
          code = Unspecified
        )
    }

    new ErrorsResponse(errors, Results.BadRequest, context.requestId)
  }

}
