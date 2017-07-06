package xyz.driver.pdsuidomain.formats.json.user

import java.util.UUID

import xyz.driver.pdsuicommon.domain._
import play.api.data.validation._
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection._
import scala.util.Try
import User._
import xyz.driver.pdsuicommon.json.JsonValidationException
import xyz.driver.pdsuicommon.validation.{AdditionalConstraints, JsonValidationErrors}

final case class ApiPartialUser(email: Option[String], name: Option[String], roleId: Option[String]) {

  def applyTo(orig: User): Try[User] = Try {
    val validation = Map(
      JsPath \ "name" -> AdditionalConstraints.optionNonEmptyConstraint(name)
    )

    val validationErrors: JsonValidationErrors = validation.collect({
      case (fieldName, e: Invalid) => (fieldName, e.errors)
    })(breakOut)

    if (validationErrors.isEmpty) {
      orig.copy(name = name.get)
    } else {
      throw new JsonValidationException(validationErrors)
    }
  }

  def toDomain(id: StringId[User] = StringId(UUID.randomUUID().toString)): Try[User] = Try {
    val validation = Map(
      JsPath \ "email"  -> AdditionalConstraints.optionNonEmptyConstraint(email),
      JsPath \ "name"   -> AdditionalConstraints.optionNonEmptyConstraint(name),
      JsPath \ "roleId" -> AdditionalConstraints.optionNonEmptyConstraint(roleId)
    )

    val validationErrors: JsonValidationErrors = validation.collect({
      case (fieldName, e: Invalid) => (fieldName, e.errors)
    })(breakOut)

    if (validationErrors.isEmpty) {
      val userEmail = email.map(x => Email(x.toLowerCase)).get
      User(
        id = id,
        email = userEmail,
        name = name.get,
        role = roleId.map(UserRole.roleFromString).get,
        passwordHash = PasswordHash(createPassword),
        latestActivity = None,
        deleted = None
      )
    } else {
      throw new JsonValidationException(validationErrors)
    }
  }
}

object ApiPartialUser {

  implicit val format: Format[ApiPartialUser] = (
    (JsPath \ "email").formatNullable[String](Format(Reads.email, Writes.StringWrites)) and
      (JsPath \ "name").formatNullable[String](
        Format(
          Reads.filterNot[String](ValidationError("Username is too long (max length is 255 chars)", 255))(
            _.length > 255),
          Writes.StringWrites
        )) and
      (JsPath \ "roleId").formatNullable[String](
        Format(Reads
                 .of[String]
                 .filter(ValidationError("unknown role"))({
                   case x if UserRole.roleFromString.isDefinedAt(x) => true
                   case _                                           => false
                 }),
               Writes.of[String]))
  )(ApiPartialUser.apply, unlift(ApiPartialUser.unapply))
}
