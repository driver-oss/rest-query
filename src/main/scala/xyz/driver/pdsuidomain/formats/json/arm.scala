package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities._

object arm {
  import DefaultJsonProtocol._
  import common._

  def applyUpdateToArm(json: JsValue, orig: Arm): Arm = json match {
    case JsObject(fields) =>
      val name = fields
        .get("name")
        .map(_.convertTo[String])
        .getOrElse(deserializationError(s"Arm json object does not contain `name` field: $json"))
      orig.copy(name = name)

    case _ => deserializationError(s"Expected Json Object as partial Arm, but got $json")
  }

  implicit val armFormat: RootJsonFormat[Arm] = new RootJsonFormat[Arm] {
    override def write(obj: Arm): JsValue =
      JsObject(
        "id"           -> obj.id.toJson,
        "name"         -> obj.name.toJson,
        "originalName" -> obj.originalName.toJson,
        "trialId"      -> obj.trialId.toJson
      )

    override def read(json: JsValue): Arm = json.asJsObject.getFields("trialId", "name") match {
      case Seq(trialId, name) =>
        Arm(
          id = json.asJsObject.fields.get("id").flatMap(_.convertTo[Option[LongId[Arm]]]).getOrElse(LongId(0)),
          name = name.convertTo[String],
          trialId = trialId.convertTo[StringId[Trial]],
          originalName = name.convertTo[String]
        )

      case _ => deserializationError(s"Expected Json Object as Arm, but got $json")
    }
  }

}
