package xyz.driver.pdsuidomain.formats.json.intervention

import play.api.data.validation.Invalid
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{Intervention, InterventionArm, InterventionWithArms, Trial}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.json.JsonValidationException
import xyz.driver.pdsuicommon.validation.{AdditionalConstraints, JsonValidationErrors}

import scala.collection.breakOut
import scala.util.Try

final case class ApiPartialIntervention(name: Option[String],
                                        trialId: Option[String],
                                        typeId: Option[Long],
                                        dosage: Option[String],
                                        isActive: Option[Boolean],
                                        arms: Option[List[Long]]) {

  def applyTo(orig: InterventionWithArms): InterventionWithArms = {
    val origIntervention = orig.intervention
    val draftArmList     = arms.map(_.map(x => InterventionArm(armId = LongId(x), interventionId = orig.intervention.id)))
    orig.copy(
      intervention = origIntervention.copy(
        name = name.getOrElse(origIntervention.name),
        typeId = typeId.map(LongId(_)).orElse(origIntervention.typeId),
        dosage = dosage.getOrElse(origIntervention.dosage),
        isActive = isActive.getOrElse(origIntervention.isActive)
      ),
      arms = draftArmList.getOrElse(orig.arms)
    )
  }

  def toDomain: Try[InterventionWithArms] = Try {
    val validation = Map(JsPath \ "trialId" -> AdditionalConstraints.optionNonEmptyConstraint(trialId))

    val validationErrors: JsonValidationErrors = validation.collect({
      case (fieldName, e: Invalid) => (fieldName, e.errors)
    })(breakOut)

    if (validationErrors.isEmpty) {
      InterventionWithArms(
        intervention = Intervention(
          id = LongId(0),
          trialId = trialId.map(StringId[Trial]).get,
          name = name.getOrElse(""),
          originalName = name.getOrElse(""),
          typeId = typeId.map(LongId(_)),
          originalType = Option(""),
          dosage = dosage.getOrElse(""),
          originalDosage = dosage.getOrElse(""),
          isActive = isActive.getOrElse(false)
        ),
        arms =
          arms.map(_.map(x => InterventionArm(armId = LongId(x), interventionId = LongId(0)))).getOrElse(List.empty)
      )
    } else {
      throw new JsonValidationException(validationErrors)
    }
  }
}

object ApiPartialIntervention {

  private val reads: Reads[ApiPartialIntervention] = (
    (JsPath \ "name").readNullable[String] and
      (JsPath \ "trialId").readNullable[String] and
      (JsPath \ "typeId").readNullable[Long] and
      (JsPath \ "dosage").readNullable[String] and
      (JsPath \ "isActive").readNullable[Boolean] and
      (JsPath \ "arms").readNullable[List[Long]]
  )(ApiPartialIntervention.apply _)

  private val writes: Writes[ApiPartialIntervention] = (
    (JsPath \ "name").writeNullable[String] and
      (JsPath \ "trialId").writeNullable[String] and
      (JsPath \ "typeId").writeNullable[Long] and
      (JsPath \ "dosage").writeNullable[String] and
      (JsPath \ "isActive").writeNullable[Boolean] and
      (JsPath \ "arms").writeNullable[List[Long]]
  )(unlift(ApiPartialIntervention.unapply))

  implicit val format: Format[ApiPartialIntervention] = Format(reads, writes)
}
