package xyz.driver.pdsuidomain.formats.json.password

import play.api.libs.json.{Format, Json}

final case class PasswordCreateRequest(password: String, key: String)

object PasswordCreateRequest {
  implicit val format: Format[PasswordCreateRequest] = Json.format[PasswordCreateRequest]
}
