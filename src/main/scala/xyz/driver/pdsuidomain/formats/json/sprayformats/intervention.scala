package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.InterventionType.DeliveryMethod
import xyz.driver.pdsuidomain.entities._

object intervention {
  import DefaultJsonProtocol._
  import common._

  implicit def interventionFormat: RootJsonFormat[InterventionWithArms] = new RootJsonFormat[InterventionWithArms] {
    override def write(obj: InterventionWithArms) =
      JsObject(
        "id"             -> obj.intervention.id.toJson,
        "name"           -> obj.intervention.name.toJson,
        "typeId"         -> obj.intervention.typeId.toJson,
        "dosage"         -> obj.intervention.dosage.toJson,
        "isActive"       -> obj.intervention.isActive.toJson,
        "arms"           -> obj.arms.map(_.armId).toJson,
        "trialId"        -> obj.intervention.trialId.toJson,
        "deliveryMethod" -> obj.intervention.deliveryMethod.toJson,
        "originalName"   -> obj.intervention.originalName.toJson,
        "originalDosage" -> obj.intervention.originalDosage.toJson,
        "originalType"   -> obj.intervention.originalType.toJson
      )

    override def read(json: JsValue): InterventionWithArms = json match {
      case JsObject(fields) =>
        val trialId = fields
          .get("trialId")
          .map(_.convertTo[StringId[Trial]])
          .getOrElse(deserializationError(s"Intervention json object does not contain `trialId` field: $json"))

        val typeId = fields
          .get("typeId")
          .flatMap(_.convertTo[Option[LongId[InterventionType]]])

        val name = fields
          .get("name")
          .map(_.convertTo[String])
          .getOrElse("")

        val dosage = fields
          .get("dosage")
          .map(_.convertTo[String])

        val isActive = fields
          .get("isActive")
          .exists(_.convertTo[Boolean])

        val deliveryMethod = fields
          .get("deliveryMethod")
          .flatMap(_.convertTo[Option[String]])

        val arms = fields
          .get("arms")
          .map(_.convertTo[List[LongId[SlotArm]]])
          .map(_ map (x => InterventionArm(armId = x, interventionId = LongId(0L))))
          .getOrElse(List.empty[InterventionArm])

        InterventionWithArms(
          intervention = Intervention(
            id = LongId(0L),
            trialId = trialId,
            name = name,
            originalName = name,
            typeId = typeId,
            originalType = None,
            dosage = dosage.getOrElse(""),
            originalDosage = dosage.getOrElse(""),
            isActive = isActive,
            deliveryMethod = deliveryMethod
          ),
          arms = arms
        )

      case _ => deserializationError(s"Expected Json Object as create Intervention json, but got $json")
    }
  }

  def applyUpdateToInterventionWithArms(json: JsValue, orig: InterventionWithArms): InterventionWithArms = json match {
    case JsObject(fields) =>
      val name = fields
        .get("name")
        .map(_.convertTo[String])

      val typeId = fields
        .get("typeId")
        .map(_.convertTo[LongId[InterventionType]])

      val dosage = fields
        .get("dosage")
        .map(_.convertTo[String])

      val isActive = fields
        .get("isActive")
        .map(_.convertTo[Boolean])

      val deliveryMethod = fields
        .get("deliveryMethod")
        .map(_.convertTo[String])

      val origIntervention = orig.intervention
      val arms = fields
        .get("arms")
        .map(_.convertTo[List[LongId[SlotArm]]].map(x => InterventionArm(x, orig.intervention.id)))

      orig.copy(
        intervention = origIntervention.copy(
          name = name.getOrElse(origIntervention.name),
          typeId = typeId.orElse(origIntervention.typeId),
          dosage = dosage.getOrElse(origIntervention.dosage),
          isActive = isActive.getOrElse(origIntervention.isActive),
          deliveryMethod = deliveryMethod.orElse(origIntervention.deliveryMethod)
        ),
        arms = arms.getOrElse(orig.arms)
      )

    case _ => deserializationError(s"Expected Json Object as partial Intervention, but got $json")
  }

  implicit def interventionTypeFormat: JsonFormat[InterventionType] = new RootJsonFormat[InterventionType] {
    override def read(json: JsValue): InterventionType = json match {
      case JsObject(fields) =>
        val name = fields
          .get("name")
          .map(_.convertTo[String])
          .getOrElse(deserializationError(s"Intervention type json object does not contain `name` field: $json"))

        InterventionType.typeFromString(name)

      case _ => deserializationError(s"Expected Json Object as Intervention type, but got $json")
    }

    override def write(obj: InterventionType) =
      JsObject(
        "id"              -> obj.id.toJson,
        "name"            -> obj.name.toJson,
        "deliveryMethods" -> obj.deliveryMethods.map(DeliveryMethod.methodToString).toJson
      )
  }

}
