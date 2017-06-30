package xyz.driver.pdsuidomain.formats.json.label

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId}
import xyz.driver.pdsuidomain.entities.{Category, ExtractedData, ExtractedDataLabel, Label}
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ApiExtractedDataLabel(id: Option[Long], categoryId: Option[Long], value: Option[String]) {

  def toDomain(dataId: LongId[ExtractedData] = LongId(0)) = ExtractedDataLabel(
    id = LongId(0),
    dataId = dataId,
    labelId = id.map(LongId[Label]),
    categoryId = categoryId.map(LongId[Category]),
    value = value.map(FuzzyValue.fromString)
  )
}

object ApiExtractedDataLabel {

  implicit val format: Format[ApiExtractedDataLabel] = (
    (JsPath \ "id").formatNullable[Long] and
      (JsPath \ "categoryId").formatNullable[Long] and
      (JsPath \ "value").formatNullable[String](
        Format(Reads
                 .of[String]
                 .filter(ValidationError("unknown value"))({
                   case x if FuzzyValue.fromString.isDefinedAt(x) => true
                   case _                                         => false
                 }),
               Writes.of[String]))
  )(ApiExtractedDataLabel.apply, unlift(ApiExtractedDataLabel.unapply))

  def fromDomain(dataLabel: ExtractedDataLabel) = ApiExtractedDataLabel(
    id = dataLabel.labelId.map(_.id),
    categoryId = dataLabel.categoryId.map(_.id),
    value = dataLabel.value.map(FuzzyValue.valueToString)
  )
}
