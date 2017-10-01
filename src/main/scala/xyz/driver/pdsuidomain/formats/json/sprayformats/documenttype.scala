package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities.DocumentType

object documenttype {
  import DefaultJsonProtocol._
  import common._

  implicit val format: RootJsonFormat[DocumentType] = jsonFormat2(DocumentType.apply)

}
