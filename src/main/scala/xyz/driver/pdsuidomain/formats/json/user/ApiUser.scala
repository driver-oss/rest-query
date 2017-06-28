package xyz.driver.pdsuidomain.formats.json.user

import java.time.{ZoneId, ZonedDateTime}

import xyz.driver.pdsuicommon.domain.User
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiUser(id: Long, email: String, name: String, roleId: String, latestActivity: Option[ZonedDateTime])

object ApiUser {

  implicit val format: Format[ApiUser] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "email").format[String](Reads.email) and
      (JsPath \ "name").format[String] and
      (JsPath \ "roleId").format[String](Format(Reads.of[String].filter(ValidationError("unknown role"))({
        case x if UserRole.roleFromString.isDefinedAt(x) => true
        case _ => false
      }), Writes.of[String])) and
      (JsPath \ "latestActivity").formatNullable[ZonedDateTime]
    ) (ApiUser.apply, unlift(ApiUser.unapply))

  def fromDomain(user: User) = ApiUser(
    user.id.id,
    user.email.value,
    user.name,
    UserRole.roleToString(user.role),
    user.latestActivity.map(ZonedDateTime.of(_, ZoneId.of("Z")))
  )
}
