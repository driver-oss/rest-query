package xyz.driver.pdsuidomain.formats.json.export

import java.time.{Instant, ZoneId}

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuicommon.domain.{StringId, UuidId}
import xyz.driver.pdsuidomain.entities.Trial
import xyz.driver.pdsuidomain.entities.export.trial.ExportTrialWithLabels

final case class ApiExportTrialWithLabels(nctId: String,
                                          trialId: String,
                                          condition: String,
                                          lastReviewed: Long,
                                          labelVersion: Long,
                                          arms: List[ApiExportTrialArm],
                                          criteria: List[ApiExportTrialLabelCriterion]) {

  def toDomain: ExportTrialWithLabels = {
    ExportTrialWithLabels(
      StringId[Trial](nctId),
      UuidId[Trial](trialId),
      condition,
      lastReviewed = Instant.ofEpochMilli(lastReviewed).atZone(ZoneId.of("Z")).toLocalDateTime,
      labelVersion,
      arms.map(_.toDomain),
      criteria.map(_.toDomain)
    )
  }
}

object ApiExportTrialWithLabels {

  implicit val format: Format[ApiExportTrialWithLabels] = (
    (JsPath \ "nctId").format[String] and
      (JsPath \ "trialId").format[String] and
      (JsPath \ "disease").format[String] and
      (JsPath \ "lastReviewed").format[Long] and
      (JsPath \ "labelVersion").format[Long] and
      (JsPath \ "arms").format[List[ApiExportTrialArm]] and
      (JsPath \ "criteria").format[List[ApiExportTrialLabelCriterion]]
  )(ApiExportTrialWithLabels.apply, unlift(ApiExportTrialWithLabels.unapply))

  def fromDomain(x: ExportTrialWithLabels) = ApiExportTrialWithLabels(
    nctId = x.nctId.id,
    trialId = x.trialId.toString,
    condition = x.condition,
    lastReviewed = x.lastReviewed.atZone(ZoneId.of("Z")).toEpochSecond,
    labelVersion = x.labelVersion,
    arms = x.arms.map(ApiExportTrialArm.fromDomain),
    criteria = x.criteria.map(ApiExportTrialLabelCriterion.fromDomain)
  )
}
