package xyz.driver.pdsuidomain.formats.json.intervention

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.InterventionType

final case class ApiInterventionType(id: Long, name: String)

object ApiInterventionType {

  implicit val format: Format[ApiInterventionType] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String]
    ) (ApiInterventionType.apply, unlift(ApiInterventionType.unapply))

  def fromDomain(interventionType: InterventionType) = ApiInterventionType(
    id = interventionType.id.id,
    name = interventionType.name
  )
}
