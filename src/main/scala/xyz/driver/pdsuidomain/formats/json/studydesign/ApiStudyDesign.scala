package xyz.driver.pdsuidomain.formats.json.studydesign

import xyz.driver.pdsuidomain.entities.StudyDesign
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuicommon.domain.LongId

final case class ApiStudyDesign(id: Long, name: String) {

  def toDomain = StudyDesign(id = LongId[StudyDesign](id), name = name)
}

object ApiStudyDesign {

  implicit val format: Format[ApiStudyDesign] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "name").format[String]
  )(ApiStudyDesign.apply, unlift(ApiStudyDesign.unapply))

  def fromDomain(studyDesign: StudyDesign) = ApiStudyDesign(
    id = studyDesign.id.id,
    name = studyDesign.name
  )
}