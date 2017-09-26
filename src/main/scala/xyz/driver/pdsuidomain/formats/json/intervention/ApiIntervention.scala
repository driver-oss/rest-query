package xyz.driver.pdsuidomain.formats.json.intervention

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{Intervention, InterventionArm, InterventionWithArms}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

final case class ApiIntervention(id: Long,
                                 name: String,
                                 typeId: Option[Long],
                                 dosage: String,
                                 isActive: Boolean,
                                 arms: List[Long],
                                 trialId: String,
                                 deliveryMethod: Option[String],
                                 originalName: String,
                                 originalDosage: String,
                                 originalType: Option[String]) {

  def toDomain = {
    val intervention = Intervention(
      id = LongId(this.id),
      trialId = StringId(this.trialId),
      name = this.name,
      originalName = this.originalName,
      typeId = this.typeId.map(id => LongId(id)),
      originalType = this.originalType.map(id => id.toString),
      dosage = this.dosage,
      originalDosage = this.originalDosage,
      isActive = this.isActive,
      deliveryMethod = this.deliveryMethod
    )

    InterventionWithArms(intervention, this.arms.map { armId =>
      InterventionArm(LongId(armId), intervention.id)
    })

  }

}

object ApiIntervention {

  implicit val format: Format[ApiIntervention] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "typeId").formatNullable[Long] and
      (JsPath \ "dosage").format[String] and
      (JsPath \ "isActive").format[Boolean] and
      (JsPath \ "arms").format[List[Long]] and
      (JsPath \ "trialId").format[String] and
      (JsPath \ "deliveryMethod").formatNullable[String] and
      (JsPath \ "originalName").format[String] and
      (JsPath \ "originalDosage").format[String] and
      (JsPath \ "originalType").formatNullable[String]
  )(ApiIntervention.apply, unlift(ApiIntervention.unapply))

  def fromDomain(interventionWithArms: InterventionWithArms): ApiIntervention = {
    import interventionWithArms.intervention
    import interventionWithArms.arms

    ApiIntervention(
      id = intervention.id.id,
      name = intervention.name,
      typeId = intervention.typeId.map(_.id),
      dosage = intervention.dosage,
      isActive = intervention.isActive,
      arms = arms.map(_.armId.id),
      trialId = intervention.trialId.id,
      deliveryMethod = intervention.deliveryMethod,
      originalName = intervention.originalName,
      originalDosage = intervention.originalDosage,
      originalType = intervention.originalType
    )
  }
}
