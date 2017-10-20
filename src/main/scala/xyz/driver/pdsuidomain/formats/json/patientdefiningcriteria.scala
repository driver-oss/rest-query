package xyz.driver.pdsuidomain.formats.json

import spray.json._
import xyz.driver.formats.json.labels._
import xyz.driver.pdsuidomain.entities.PatientLabel

object patientdefiningcriteria {
  import DefaultJsonProtocol._
  import common._

  implicit val patientLabelDefiningCriteriaWriter: RootJsonWriter[PatientLabel] = new RootJsonWriter[PatientLabel] {
    override def write(obj: PatientLabel) =
      JsObject(
        "id"    -> obj.id.toJson,
        "value" -> obj.verifiedPrimaryValue.toJson
      )
  }

}
