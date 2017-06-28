package xyz.driver.pdsuidomain.formats.json.category

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.CategoryWithLabels
import xyz.driver.pdsuidomain.formats.json.label.ApiLabel

final case class ApiCategory(id: Long, name: String, labels: List[ApiLabel])

object ApiCategory {

  implicit val format: Format[ApiCategory] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "labels").format[List[ApiLabel]]
    ) (ApiCategory.apply, unlift(ApiCategory.unapply))

  def fromDomain(categoryWithLabels: CategoryWithLabels) = ApiCategory(
    id = categoryWithLabels.category.id.id,
    name = categoryWithLabels.category.name,
    labels = categoryWithLabels.labels.map(x => ApiLabel(x.id.id, x.name, x.categoryId.id))
  )
}
