package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{SlotArm, Trial}

object slotarm {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToArm(json: JsValue, orig: SlotArm): SlotArm = json match {
    case JsObject(fields) =>
      val name = fields
        .get("name")
        .map(_.convertTo[String])
        .getOrElse(deserializationError(s"Arm json object does not contain `name` field: $json"))
      orig.copy(name = name)

    case _ => deserializationError(s"Expected Json Object as partial Arm, but got $json")
  }

  implicit def slotArmFormat: RootJsonFormat[SlotArm] = new RootJsonFormat[SlotArm] {
    override def write(obj: SlotArm): JsValue =
      JsObject(
        "id"           -> obj.id.toJson,
        "name"         -> obj.name.toJson,
        "originalName" -> obj.originalName.toJson,
        "trialId"      -> obj.trialId.toJson
      )

    override def read(json: JsValue): SlotArm = json.asJsObject.getFields("trialId", "name") match {
      case Seq(trialId, name) =>
        SlotArm(
          id = LongId(0),
          name = name.convertTo[String],
          trialId = trialId.convertTo[StringId[Trial]],
          originalName = name.convertTo[String]
        )

      case _ => deserializationError(s"Expected Json Object as Arm, but got $json")
    }
  }

}
