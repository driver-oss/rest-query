package xyz.driver.pdsuidomain.formats.json.intervention

import xyz.driver.pdsuidomain.entities.InterventionWithArms
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

final case class ApiIntervention(id: Long,
                                 name: String,
                                 typeId: Option[Long],
                                 description: String,
                                 isActive: Boolean,
                                 arms: List[Long],
                                 trialId: String)

object ApiIntervention {

  implicit val format: Format[ApiIntervention] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "typeId").formatNullable[Long] and
      (JsPath \ "description").format[String] and
      (JsPath \ "isActive").format[Boolean] and
      (JsPath \ "arms").format[List[Long]] and
      (JsPath \ "trialId").format[String]
    ) (ApiIntervention.apply, unlift(ApiIntervention.unapply))

  def fromDomain(interventionWithArms: InterventionWithArms): ApiIntervention = {
    import interventionWithArms.intervention
    import interventionWithArms.arms

    ApiIntervention(
      id = intervention.id.id,
      name = intervention.name,
      typeId = intervention.typeId.map(_.id),
      description = intervention.description,
      isActive = intervention.isActive,
      arms = arms.map(_.armId.id),
      trialId = intervention.trialId.id
    )
  }
}
