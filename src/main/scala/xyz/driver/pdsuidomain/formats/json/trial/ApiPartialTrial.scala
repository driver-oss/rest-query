package xyz.driver.pdsuidomain.formats.json.trial

import java.util.UUID

import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuidomain.entities.Trial
import org.davidbild.tristate.Tristate
import org.davidbild.tristate.contrib.play.ToJsPathOpsFromJsPath
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiPartialTrial(hypothesisId: Tristate[UUID],
                                 studyDesignId: Tristate[Long],
                                 overview: Tristate[String],
                                 title: Tristate[String]) {

  def applyTo(orig: Trial): Trial = {
    orig.copy(
      hypothesisId = hypothesisId.map(UuidId(_)).cata(Some(_), None, orig.hypothesisId),
      studyDesignId = studyDesignId.map(LongId(_)).cata(Some(_), None, orig.studyDesignId),
      overview = overview.cata(Some(_), None, orig.overview),
      title = title.cata(Some(_).getOrElse(""), "", orig.title)
    )
  }
}

object ApiPartialTrial {

  private val reads: Reads[ApiPartialTrial] = (
    (JsPath \ "hypothesisId").readTristate[UUID] and
      (JsPath \ "studyDesignId").readTristate[Long] and
      (JsPath \ "overview").readTristate[String] and
      (JsPath \ "title").readTristate[String]
  )(ApiPartialTrial.apply _)

  private val writes: Writes[ApiPartialTrial] = (
    (JsPath \ "hypothesisId").writeTristate[UUID] and
      (JsPath \ "studyDesignId").writeTristate[Long] and
      (JsPath \ "overview").writeTristate[String] and
      (JsPath \ "title").writeTristate[String]
  )(unlift(ApiPartialTrial.unapply))

  implicit val format: Format[ApiPartialTrial] = Format(reads, writes)
}
