package xyz.driver.pdsuidomain.formats.json.extracteddata

import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuidomain.entities.ExtractedData
import xyz.driver.pdsuidomain.formats.json.label.ApiExtractedDataLabel
import play.api.libs.json._
import play.api.data.validation._
import play.api.libs.functional.syntax._
import xyz.driver.pdsuicommon.json.JsonSerializer
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData

// The specification: https://driverinc.atlassian.net/wiki/pages/viewpage.action?pageId=33423387
// Note, that there is "Extracted data object or Temporary extracted data object" in specification
// ApiExtractedData represents both types
final case class ApiExtractedData(id: Long,
                                  documentId: Long,
                                  keywordId: Option[Long],
                                  evidence: Option[String],
                                  meta: Option[String],
                                  // An empty list and no-existent list are different cases
                                  labels: Option[List[ApiExtractedDataLabel]]) {

  def toDomain = RichExtractedData(
    extractedData = ExtractedData(
      id = LongId(this.id),
      documentId = LongId(this.documentId),
      keywordId = this.keywordId.map(LongId(_)),
      evidenceText = this.evidence,
      meta = this.meta.map(x => TextJson(JsonSerializer.deserialize[ExtractedData.Meta](x)))
    ),
    labels = labels.getOrElse(List.empty).map(_.toDomain())
  )

}

object ApiExtractedData {

  implicit val format: Format[ApiExtractedData] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "documentId").format[Long] and
      (JsPath \ "keywordId").formatNullable[Long] and
      (JsPath \ "evidence").formatNullable[String] and
      (JsPath \ "meta").formatNullable[String] and
      (JsPath \ "labels").formatNullable[List[ApiExtractedDataLabel]](
        Format(
          Reads
            .of[List[ApiExtractedDataLabel]]
            .filter(ValidationError("empty labels"))({
              case x if x.nonEmpty => true
              case _               => false
            }),
          Writes.of[List[ApiExtractedDataLabel]]
        ))
  )(ApiExtractedData.apply, unlift(ApiExtractedData.unapply))

  def fromDomain(extractedDataWithLabels: RichExtractedData) = ApiExtractedData(
    id = extractedDataWithLabels.extractedData.id.id,
    documentId = extractedDataWithLabels.extractedData.documentId.id,
    keywordId = extractedDataWithLabels.extractedData.keywordId.map(_.id),
    evidence = extractedDataWithLabels.extractedData.evidenceText,
    meta = extractedDataWithLabels.extractedData.meta.map(x => JsonSerializer.serialize(x.content)),
    labels = Option(extractedDataWithLabels.labels.map(ApiExtractedDataLabel.fromDomain))
  )
}
