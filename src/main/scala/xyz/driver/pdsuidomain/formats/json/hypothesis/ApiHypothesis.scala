package xyz.driver.pdsuidomain.formats.json.hypothesis

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.entities.Hypothesis

final case class ApiHypothesis(id: UUID, name: String, treatmentType: String, description: String) {

  def toDomain = Hypothesis(
    id = UuidId[Hypothesis](id),
    name = name,
    treatmentType = treatmentType,
    description = description
  )
}

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
