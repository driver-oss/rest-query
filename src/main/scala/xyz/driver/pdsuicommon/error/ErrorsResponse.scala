package xyz.driver.pdsuicommon.error

import spray.json._
import ErrorsResponse.ResponseError

final case class ErrorsResponse(errors: Seq[ResponseError], requestId: String)

object ErrorsResponse {
  import DefaultJsonProtocol._

  /**
    * @param data      Any data that can be associated with particular error.Ex.: error field name
    * @param message   Error message
    * @param code      Unique error code
    *
    * @see https://driverinc.atlassian.net/wiki/display/RA/REST+API+Specification#RESTAPISpecification-HTTPStatuscodes
    */
  final case class ResponseError(data: Option[String], message: String, code: Int)

  object ResponseError {

    implicit val responseErrorJsonFormat: RootJsonFormat[ResponseError] = jsonFormat3(ResponseError.apply)

  }

  implicit val errorsResponseJsonFormat: RootJsonFormat[ErrorsResponse] = new RootJsonFormat[ErrorsResponse] {
    override def write(obj: ErrorsResponse): JsValue = {
      JsObject(
        "errors"    -> obj.errors.map(_.toJson).toJson,
        "requestId" -> obj.requestId.toJson
      )
    }

    override def read(json: JsValue): ErrorsResponse = json match {
      case JsObject(fields) =>
        val errors = fields
          .get("errors")
          .map(_.convertTo[Seq[ResponseError]])
          .getOrElse(deserializationError(s"ErrorsResponse json object does not contain `errors` field: $json"))

        val requestId = fields
          .get("requestId")
          .map(id => id.convertTo[String])
          .getOrElse(deserializationError(s"ErrorsResponse json object does not contain `requestId` field: $json"))

        ErrorsResponse(errors, requestId)

      case _ => deserializationError(s"Expected json as ErrorsResponse, but got $json")
    }
  }

}
