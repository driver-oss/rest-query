package xyz.driver.pdsuidomain.formats.json.intervention

import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities.{InterventionArm, InterventionWithArms}
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiPartialIntervention(typeId: Option[Long],
                                        dosage: Option[String],
                                        isActive: Option[Boolean],
                                        arms: Option[List[Long]]) {

  def applyTo(orig: InterventionWithArms): InterventionWithArms = {
    val origIntervention = orig.intervention
    val draftArmList     = arms.map(_.map(x => InterventionArm(LongId(x), orig.intervention.id)))
    orig.copy(
      intervention = origIntervention.copy(
        typeId = typeId.map(LongId(_)).orElse(origIntervention.typeId),
        dosage = dosage.getOrElse(origIntervention.dosage),
        isActive = isActive.getOrElse(origIntervention.isActive)
      ),
      arms = draftArmList.getOrElse(orig.arms)
    )
  }
}

object ApiPartialIntervention {

  private val reads: Reads[ApiPartialIntervention] = (
    (JsPath \ "typeId").readNullable[Long] and
      (JsPath \ "dosage").readNullable[String] and
      (JsPath \ "isActive").readNullable[Boolean] and
      (JsPath \ "arms").readNullable[List[Long]]
  )(ApiPartialIntervention.apply _)

  private val writes: Writes[ApiPartialIntervention] = (
    (JsPath \ "typeId").writeNullable[Long] and
      (JsPath \ "dosage").writeNullable[String] and
      (JsPath \ "isActive").writeNullable[Boolean] and
      (JsPath \ "arms").writeNullable[List[Long]]
  )(unlift(ApiPartialIntervention.unapply))

  implicit val format: Format[ApiPartialIntervention] = Format(reads, writes)
}
