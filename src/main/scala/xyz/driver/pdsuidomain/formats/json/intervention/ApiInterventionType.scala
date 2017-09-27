package xyz.driver.pdsuidomain.formats.json.intervention

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.InterventionType
import xyz.driver.pdsuidomain.entities.InterventionType.DeliveryMethod

final case class ApiInterventionType(id: Long, name: String, deliveryMethods: List[String]) {

  def toDomain = InterventionType.typeFromString(name)
}

object ApiInterventionType {

  implicit val format: Format[ApiInterventionType] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "deliveryMethods").format[List[String]]
  )(ApiInterventionType.apply, unlift(ApiInterventionType.unapply))

  def fromDomain(interventionType: InterventionType) = ApiInterventionType(
    id = interventionType.id.id,
    name = interventionType.name,
    deliveryMethods = interventionType.deliveryMethod.map(DeliveryMethod.methodToString).toList
  )
}
