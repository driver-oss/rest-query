package xyz.driver.pdsuidomain.formats.json.label

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId}
import xyz.driver.pdsuidomain.entities.{Criterion, CriterionLabel}
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.entities.labels.{Label, LabelCategory}

/**
  * @param value Yes|No
  */
final case class ApiCriterionLabel(labelId: Option[Long],
                                   categoryId: Option[Long],
                                   value: Option[String],
                                   isDefining: Boolean) {

  def toDomain(criterionId: LongId[Criterion]) = CriterionLabel(
    id = LongId(0L),
    labelId = labelId.map(LongId[Label]),
    criterionId = criterionId,
    categoryId = categoryId.map(LongId[LabelCategory]),
    value = value.map {
      case "Yes" => true
      case "No"  => false
    },
    isDefining = isDefining
  )
}

object ApiCriterionLabel {

  def fromDomain(x: CriterionLabel) = ApiCriterionLabel(
    labelId = x.labelId.map(_.id),
    categoryId = x.categoryId.map(_.id),
    value = x.value.map { x =>
      FuzzyValue.valueToString(FuzzyValue.fromBoolean(x))
    },
    isDefining = x.isDefining
  )

  implicit val format: Format[ApiCriterionLabel] = (
    (JsPath \ "labelId").formatNullable[Long] and
      (JsPath \ "categoryId").formatNullable[Long] and
      (JsPath \ "value").formatNullable[String](
        Format(Reads
                 .of[String]
                 .filter(ValidationError("unknown value"))({ x =>
                   x == "Yes" || x == "No"
                 }),
               Writes.of[String])) and
      (JsPath \ "isDefining").format[Boolean]
  )(ApiCriterionLabel.apply, unlift(ApiCriterionLabel.unapply))
}
