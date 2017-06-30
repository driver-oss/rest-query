package xyz.driver.pdsuidomain.formats.json.keyword

import xyz.driver.pdsuidomain.entities.KeywordWithLabels
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.formats.json.label.ApiLabel

final case class ApiKeyword(id: Long, keyword: String, labels: List[ApiLabel])

object ApiKeyword {

  implicit val format: Format[ApiKeyword] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "keyword").format[String] and
      (JsPath \ "labels").format[List[ApiLabel]]
  )(ApiKeyword.apply, unlift(ApiKeyword.unapply))

  def fromDomain(keywordWithLabels: KeywordWithLabels) = ApiKeyword(
    id = keywordWithLabels.keyword.id.id,
    keyword = keywordWithLabels.keyword.keyword,
    labels = keywordWithLabels.labels.map(x => ApiLabel(x.id.id, x.name, x.categoryId.id))
  )
}
