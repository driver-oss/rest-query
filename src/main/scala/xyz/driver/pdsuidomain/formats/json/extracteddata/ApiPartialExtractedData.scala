package xyz.driver.pdsuidomain.formats.json.extracteddata

import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuidomain.entities.ExtractedData.Meta
import xyz.driver.pdsuidomain.entities._
import org.davidbild.tristate.Tristate
import org.davidbild.tristate.contrib.play.ToJsPathOpsFromJsPath
import play.api.data.validation._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.json.{JsonSerializer, JsonValidationException}
import xyz.driver.pdsuicommon.validation.{AdditionalConstraints, JsonValidationErrors}
import xyz.driver.pdsuidomain.formats.json.label.ApiExtractedDataLabel
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData

import scala.collection._

final case class ApiPartialExtractedData(documentId: Option[Long],
                                         keywordId: Option[Long],
                                         evidence: Tristate[String],
                                         meta: Tristate[String],
                                         labels: Tristate[List[ApiExtractedDataLabel]]) {

  def applyTo(orig: RichExtractedData): RichExtractedData = RichExtractedData(
    extractedData = applyTo(orig.extractedData),
    labels = labels.cata(_.map(_.toDomain(orig.extractedData.id)), List.empty, orig.labels)
  )

  private def applyTo(orig: ExtractedData): ExtractedData = ExtractedData(
    id = orig.id,
    documentId = orig.documentId,
    keywordId = keywordId.map(LongId[Keyword]).orElse(orig.keywordId),
    evidenceText = evidence.cata(Some(_), None, orig.evidenceText),
    meta = meta.map(x => TextJson(JsonSerializer.deserialize[Meta](x))).cata(Some(_), None, orig.meta)
  )

  def toDomain: RichExtractedData = {
    val validation = Map(
      JsPath \ "documentId" -> AdditionalConstraints.optionNonEmptyConstraint(documentId)
    )

    val validationErrors: JsonValidationErrors = validation.collect({
      case (fieldName, e: Invalid) => (fieldName, e.errors)
    })(breakOut)

    if (validationErrors.isEmpty) {
      val extractedData = ExtractedData(
        documentId = documentId.map(LongId[Document]).get,
        keywordId = keywordId.map(LongId[Keyword]),
        evidenceText = evidence.toOption,
        meta = meta.map(x => TextJson(JsonSerializer.deserialize[Meta](x))).toOption
      )
      val labelList = labels.map(_.map(_.toDomain()))
      RichExtractedData(extractedData, labelList.getOrElse(List.empty))
    } else {
      throw new JsonValidationException(validationErrors)
    }
  }
}

object ApiPartialExtractedData {

  private val reads: Reads[ApiPartialExtractedData] = (
    (JsPath \ "documentId").readNullable[Long] and
      (JsPath \ "keywordId").readNullable[Long] and
      (JsPath \ "evidence").readTristate[String] and
      (JsPath \ "meta").readTristate[String] and
      (JsPath \ "labels").readTristate[List[ApiExtractedDataLabel]]
    ) (ApiPartialExtractedData.apply _)

  private val writes: Writes[ApiPartialExtractedData] = (
    (JsPath \ "documentId").writeNullable[Long] and
      (JsPath \ "keywordId").writeNullable[Long] and
      (JsPath \ "evidence").writeTristate[String] and
      (JsPath \ "meta").writeTristate[String] and
      (JsPath \ "labels").writeTristate[List[ApiExtractedDataLabel]]
    ) (unlift(ApiPartialExtractedData.unapply))

  implicit val format: Format[ApiPartialExtractedData] = Format(reads, writes)
}
