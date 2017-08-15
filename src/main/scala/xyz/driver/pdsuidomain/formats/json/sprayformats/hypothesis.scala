package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities._

object hypothesis {
  import DefaultJsonProtocol._
  import common._

  implicit val hypothesisFormat: RootJsonFormat[Hypothesis] = jsonFormat4(Hypothesis.apply)

}
