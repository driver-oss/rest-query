package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, TextJson}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData

object extracteddata {
  import DefaultJsonProtocol._
  import common._
  import ExtractedData._

  implicit val metaKeywordFormat: RootJsonFormat[Meta.Keyword] = jsonFormat4(Meta.Keyword)
  implicit val metaTextLayerPositionFormat: RootJsonFormat[Meta.TextLayerPosition] = jsonFormat3(
    Meta.TextLayerPosition)
  implicit val metaEvidenceFormat: RootJsonFormat[Meta.Evidence] = jsonFormat3(Meta.Evidence)

  implicit val extractedDataMetaFormat: RootJsonFormat[Meta] = jsonFormat2(Meta.apply)
  implicit val fullExtractedDataMetaFormat = new RootJsonFormat[TextJson[Meta]] {
    override def write(obj: TextJson[Meta]): JsValue = obj.content.toJson
    override def read(json: JsValue): TextJson[Meta] = TextJson(extractedDataMetaFormat.read(json))
  }

  implicit val extractedDataLabelFormat: RootJsonFormat[ExtractedDataLabel] = new RootJsonFormat[ExtractedDataLabel] {
    override def write(label: ExtractedDataLabel): JsObject = {
      JsObject(
        "id"         -> label.labelId.toJson,
        "categoryId" -> label.categoryId.toJson,
        "value"      -> label.value.toJson
      )
    }

    override def read(json: JsValue): ExtractedDataLabel = json match {
      case JsObject(fields) =>
        val labelId = fields
          .get("id")
          .map(_.convertTo[LongId[Label]])

        val categoryId = fields
          .get("categoryId")
          .map(_.convertTo[LongId[Category]])

        val value = fields
          .get("value")
          .map(_.convertTo[FuzzyValue])

        ExtractedDataLabel(
          id = LongId(0),
          dataId = LongId(0),
          labelId = labelId,
          categoryId = categoryId,
          value = value
        )

      case _ => deserializationError(s"Expected Json Object as ExtractedDataLabel, but got $json")
    }
  }

  def applyUpdateToExtractedData(json: JsValue, orig: RichExtractedData): RichExtractedData = json match {
    case JsObject(fields) =>
      val keywordId = if (fields.contains("keywordId")) {
        fields
          .get("keywordId")
          .map(_.convertTo[LongId[Keyword]])
      } else orig.extractedData.keywordId

      val evidence = if (fields.contains("evidence")) {
        fields
          .get("evidence")
          .map(_.convertTo[String])
      } else orig.extractedData.evidenceText

      val meta = if (fields.contains("meta")) {
        fields
          .get("meta")
          .map(_.convertTo[TextJson[Meta]])
      } else orig.extractedData.meta

      val labels = if (fields.contains("labels")) {
        fields
          .get("labels")
          .map(_.convertTo[List[ExtractedDataLabel]])
          .getOrElse(List.empty[ExtractedDataLabel])
      } else orig.labels

      val extractedData = orig.extractedData.copy(
        keywordId = keywordId,
        evidenceText = evidence,
        meta = meta
      )

      orig.copy(
        extractedData = extractedData,
        labels = labels
      )

    case _ => deserializationError(s"Expected Json Object as partial ExtractedData, but got $json")
  }

  implicit val extractedDataFormat: RootJsonFormat[RichExtractedData] = new RootJsonFormat[RichExtractedData] {
    override def write(richData: RichExtractedData): JsValue =
      JsObject(
        "id"         -> richData.extractedData.id.id.toJson,
        "documentId" -> richData.extractedData.documentId.toJson,
        "keywordId"  -> richData.extractedData.keywordId.toJson,
        "evidence"   -> richData.extractedData.evidenceText.toJson,
        "meta"       -> richData.extractedData.meta.toJson,
        "labels"     -> richData.labels.toJson
      )

    override def read(json: JsValue): RichExtractedData = json match {
      case JsObject(fields) =>
        val documentId = fields
          .get("documentId")
          .map(_.convertTo[LongId[Document]])
          .getOrElse(
            deserializationError(s"ExtractedData create json object does not contain `documentId` field: $json"))

        val keywordId = fields
          .get("keywordId")
          .map(_.convertTo[LongId[Keyword]])

        val evidence = fields
          .get("evidence")
          .map(_.convertTo[String])

        val meta = fields
          .get("meta")
          .map(_.convertTo[TextJson[Meta]])

        val labels = fields
          .get("labels")
          .map(_.convertTo[List[ExtractedDataLabel]])
          .getOrElse(List.empty[ExtractedDataLabel])

        val extractedData = ExtractedData(
          documentId = documentId,
          keywordId = keywordId,
          evidenceText = evidence,
          meta = meta
        )

        RichExtractedData(extractedData, labels)

      case _ => deserializationError(s"Expected Json Object as ExtractedData, but got $json")
    }
  }

}
