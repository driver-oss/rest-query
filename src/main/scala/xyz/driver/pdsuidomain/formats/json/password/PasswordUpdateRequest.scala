package xyz.driver.pdsuidomain.formats.json.password

import play.api.libs.json.{Format, Json}

final case class PasswordUpdateRequest(password: String, oldPassword: String)

object PasswordUpdateRequest {
  implicit val format: Format[PasswordUpdateRequest] = Json.format[PasswordUpdateRequest]
}
