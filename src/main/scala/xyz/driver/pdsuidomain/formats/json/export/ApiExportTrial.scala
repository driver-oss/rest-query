package xyz.driver.pdsuidomain.formats.json.export

import java.time.ZoneId

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.export.trial.ExportTrial

final case class ApiExportTrial(nctId: String, trialId: String, disease: String, lastReviewed: Long)

object ApiExportTrial {

  implicit val format: Format[ApiExportTrial] = (
    (JsPath \ "nctId").format[String] and
      (JsPath \ "trialId").format[String] and
      (JsPath \ "disease").format[String] and
      (JsPath \ "lastReviewed").format[Long]
    ) (ApiExportTrial.apply, unlift(ApiExportTrial.unapply))

  def fromDomain(trial: ExportTrial): ApiExportTrial = ApiExportTrial(
    nctId = trial.nctId.id,
    trialId = trial.trialId.toString,
    disease = trial.condition.toString.toUpperCase,
    lastReviewed = trial.lastReviewed.atZone(ZoneId.of("Z")).toEpochSecond
  )
}
