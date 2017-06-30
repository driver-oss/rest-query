package xyz.driver.pdsuidomain.formats.json.label

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.Label

final case class ApiLabel(id: Long, name: String, categoryId: Long)

object ApiLabel {

  implicit val format: Format[ApiLabel] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "categoryId").format[Long]
  )(ApiLabel.apply, unlift(ApiLabel.unapply))

  def fromDomain(x: Label) = ApiLabel(
    id = x.id.id,
    name = x.name,
    categoryId = x.categoryId.id
  )
}
