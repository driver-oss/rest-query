package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuidomain.entities.ProviderType

object providertype {
  import DefaultJsonProtocol._
  import common._

  implicit val providerTypeFormat: RootJsonFormat[ProviderType] = jsonFormat2(ProviderType.apply)

}
