package xyz.driver.pdsuidomain.formats.json.session

import play.api.libs.json.Json
import xyz.driver.pdsuidomain.formats.json.user.ApiUser

final case class NewSessionResponse(token: String, user: ApiUser)

object NewSessionResponse {

  implicit val format = Json.format[NewSessionResponse]
}
