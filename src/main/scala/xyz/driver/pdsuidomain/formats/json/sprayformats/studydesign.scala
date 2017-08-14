package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities._

object studydesign {
  import DefaultJsonProtocol._
  import common._

  implicit val studyDesignFormat: RootJsonFormat[StudyDesign] = jsonFormat2(StudyDesign.apply)

}
