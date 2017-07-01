package xyz.driver.pdsuidomain.formats.json.export

import xyz.driver.pdsuicommon.domain.FuzzyValue
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuidomain.entities.export.trial.ExportTrialLabelCriterion

final case class ApiExportTrialLabelCriterion(value: String,
                                              labelId: String,
                                              criterionId: String,
                                              criterionText: String,
                                              armIds: List[String],
                                              isCompound: Boolean,
                                              isDefining: Boolean)

object ApiExportTrialLabelCriterion {

  implicit val format: Format[ApiExportTrialLabelCriterion] = (
    (JsPath \ "value").format[String](Writes[String](x => JsString(x.toUpperCase))) and
      (JsPath \ "labelId").format[String] and
      (JsPath \ "criterionId").format[String] and
      (JsPath \ "criterionText").format[String] and
      (JsPath \ "armIds").format[List[String]] and
      (JsPath \ "isCompound").format[Boolean] and
      (JsPath \ "isDefining").format[Boolean]
    ) (ApiExportTrialLabelCriterion.apply, unlift(ApiExportTrialLabelCriterion.unapply))

  def fromDomain(x: ExportTrialLabelCriterion) = ApiExportTrialLabelCriterion(
    value = x.value.map { x =>
      FuzzyValue.valueToString(FuzzyValue.fromBoolean(x))
    }.getOrElse("Unknown"),
    labelId = x.labelId.toString,
    criterionId = x.criterionId.toString,
    criterionText = x.criteria,
    armIds = x.armIds.map(_.toString).toList,
    isCompound = x.isCompound,
    isDefining = x.isDefining
  )
}
