package xyz.driver.pdsuidomain.formats.json.hypothesis

import java.util.UUID

import xyz.driver.pdsuidomain.entities.Hypothesis
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

final case class ApiHypothesis(id: UUID, name: String, treatmentType: String, description: String)

object ApiHypothesis {

  implicit val format: Format[ApiHypothesis] = (
    (JsPath \ "id").format[UUID] and
      (JsPath \ "name").format[String] and
      (JsPath \ "treatmentType").format[String] and
      (JsPath \ "description").format[String]
  )(ApiHypothesis.apply, unlift(ApiHypothesis.unapply))

  def fromDomain(hypothesis: Hypothesis) = ApiHypothesis(
    id = hypothesis.id.id,
    name = hypothesis.name,
    treatmentType = hypothesis.treatmentType,
    description = hypothesis.description
  )
}
