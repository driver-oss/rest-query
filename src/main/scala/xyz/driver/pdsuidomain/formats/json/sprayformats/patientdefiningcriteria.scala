package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities.PatientLabel

object patientdefiningcriteria {
  import DefaultJsonProtocol._
  import common._

  implicit val patientLabelDefiningCriteriaWriter: JsonWriter[PatientLabel] = new JsonWriter[PatientLabel] {
    override def write(obj: PatientLabel) =
      JsObject(
        "id"    -> obj.id.toJson,
        "value" -> obj.verifiedPrimaryValue.toJson
      )
  }

}
