package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuidomain.entities._

object intervention {
  import DefaultJsonProtocol._
  import common._

  implicit val interventionWriter: JsonWriter[InterventionWithArms] = new JsonWriter[InterventionWithArms] {
    override def write(obj: InterventionWithArms) =
      JsObject(
        "id"             -> obj.intervention.id.toJson,
        "name"           -> obj.intervention.name.toJson,
        "typeId"         -> obj.intervention.typeId.toJson,
        "dosage"         -> obj.intervention.dosage.toJson,
        "isActive"       -> obj.intervention.isActive.toJson,
        "arms"           -> obj.arms.map(_.armId).toJson,
        "trialId"        -> obj.intervention.trialId.toJson,
        "originalName"   -> obj.intervention.originalName.toJson,
        "originalDosage" -> obj.intervention.originalDosage.toJson,
        "originalType"   -> obj.intervention.originalType.toJson
      )
  }

  def applyUpdateToInterventionWithArms(json: JsValue, orig: InterventionWithArms): InterventionWithArms = json match {
    case JsObject(fields) =>
      val typeId = fields
        .get("typeId")
        .map(_.convertTo[LongId[InterventionType]])

      val dosage = fields
        .get("dosage")
        .map(_.convertTo[String])

      val isActive = fields
        .get("isActive")
        .map(_.convertTo[Boolean])

      val origIntervention = orig.intervention
      val arms = fields
        .get("arms")
        .map(_.convertTo[List[LongId[Arm]]].map(x => InterventionArm(x, orig.intervention.id)))

      orig.copy(
        intervention = origIntervention.copy(
          typeId = typeId.orElse(origIntervention.typeId),
          dosage = dosage.getOrElse(origIntervention.dosage),
          isActive = isActive.getOrElse(origIntervention.isActive)
        ),
        arms = arms.getOrElse(orig.arms)
      )

    case _ => deserializationError(s"Expected Json Object as partial Intervention, but got $json")
  }

  implicit val interventionTypeFormat: RootJsonFormat[InterventionType] = jsonFormat2(InterventionType.apply)

}
