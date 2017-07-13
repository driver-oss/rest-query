package xyz.driver.pdsuidomain.formats.json.user

import java.time.{ZoneId, ZonedDateTime}

import xyz.driver.pdsuicommon.domain.User
import xyz.driver.pdsuicommon.json.Serialization.seqJsonFormat
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection.Seq

final case class ApiUser(id: String,
                         email: String,
                         name: String,
                         roles: Seq[String],
                         latestActivity: Option[ZonedDateTime])

object ApiUser {

  implicit val format: Format[ApiUser] = (
    (JsPath \ "id").format[String] and
      (JsPath \ "email").format[String](Reads.email) and
      (JsPath \ "name").format[String] and
      (JsPath \ "roles").format(
        Format(
          seqJsonFormat[String].filter(ValidationError("unknown role"))(_.forall(UserRole.roleFromString.isDefinedAt)),
          Writes.of[Seq[String]])) and
      (JsPath \ "latestActivity").formatNullable[ZonedDateTime]
  )(ApiUser.apply, unlift(ApiUser.unapply))

  def fromDomain(user: User) = ApiUser(
    user.id.id,
    user.email.value,
    user.name,
    user.roles.map(UserRole.roleToString).toSeq,
    user.latestActivity.map(ZonedDateTime.of(_, ZoneId.of("Z")))
  )
}
