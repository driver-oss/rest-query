package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities._

object studydesign {
  import DefaultJsonProtocol._
  import common._

  implicit val studyDesignFormat: RootJsonFormat[StudyDesign] = new RootJsonFormat[StudyDesign] {
    override def read(json: JsValue): StudyDesign = json match {
      case JsObject(fields) =>
        val name = fields
          .get("name")
          .map(_.convertTo[String])
          .getOrElse(deserializationError(s"Study design json object does not contain `name` field: $json"))

        StudyDesign
          .fromString(name)
          .getOrElse(deserializationError(s"Unknown study design: $name"))

      case _ => deserializationError(s"Expected Json Object as Study design, but got $json")
    }

    override def write(obj: StudyDesign) =
      JsObject("id" -> obj.id.toJson, "name" -> obj.name.toJson)
  }

}
