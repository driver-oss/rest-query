package xyz.driver.pdsuidomain.formats.json.session

import xyz.driver.pdsuicommon.domain.Email
import play.api.libs.json._
import xyz.driver.pdsuicommon.json.Serialization._

final case class NewSessionRequest(email: Email, password: String)

object NewSessionRequest {

  implicit val format: Format[NewSessionRequest] = Json.format[NewSessionRequest]
}
