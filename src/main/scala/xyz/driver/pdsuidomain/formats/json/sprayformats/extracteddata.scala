package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.entities.labels.{Label, LabelCategory, LabelValue}
import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.ExtractedDataService.RichExtractedData
import xyz.driver.formats.json.labels._

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

  implicit val extractedDataLabelWriter: JsonWriter[ExtractedDataLabel] = new JsonWriter[ExtractedDataLabel] {
    override def write(label: ExtractedDataLabel): JsObject = {
      JsObject(
        "id"         -> label.labelId.toJson,
        "categoryId" -> label.categoryId.toJson,
        "value"      -> label.value.toJson
      )
    }
  }

  def applyLabelsForExtractedData(json: JsValue, dataId: LongId[ExtractedData]): ExtractedDataLabel = json match {
    case JsObject(fields) =>
      val labelId = fields
        .get("id")
        .flatMap(_.convertTo[Option[LongId[Label]]])

      val categoryId = fields
        .get("categoryId")
        .flatMap(_.convertTo[Option[LongId[LabelCategory]]])

      val value = fields
        .get("value")
        .flatMap(_.convertTo[Option[LabelValue]])

      ExtractedDataLabel(
        id = LongId(0),
        dataId = dataId,
        labelId = labelId,
        categoryId = categoryId,
        value = value
      )

    case _ => deserializationError(s"Expected Json Object as ExtractedDataLabel, but got $json")
  }

  def applyUpdateToExtractedData(json: JsValue, orig: RichExtractedData): RichExtractedData = json match {
    case JsObject(fields) =>
      val keywordId = fields
        .get("keywordId")
        .map(_.convertTo[Option[LongId[Keyword]]])
        .getOrElse(orig.extractedData.keywordId)

      val evidence = fields
        .get("evidence")
        .map(_.convertTo[Option[String]])
        .getOrElse(orig.extractedData.evidenceText)

      val meta = fields
        .get("meta")
        .map(_.convertTo[Option[TextJson[Meta]]])
        .getOrElse(orig.extractedData.meta)

      val labels = fields
        .get("labels")
        .map(
          _.convertTo[Option[List[JsValue]]]
            .getOrElse(List.empty[JsValue])
            .map(l => applyLabelsForExtractedData(l, orig.extractedData.id)))
        .getOrElse(orig.labels)

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
        "id"         -> richData.extractedData.id.toJson,
        "documentId" -> richData.extractedData.documentId.toJson,
        "keywordId"  -> richData.extractedData.keywordId.toJson,
        "evidence"   -> richData.extractedData.evidenceText.toJson,
        "meta"       -> richData.extractedData.meta.toJson,
        "labels"     -> richData.labels.map(_.toJson).toJson
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
          .flatMap(_.convertTo[Option[LongId[Keyword]]])

        val evidence = fields
          .get("evidence")
          .flatMap(_.convertTo[Option[String]])

        val meta = fields
          .get("meta")
          .flatMap(_.convertTo[Option[TextJson[Meta]]])

        val labels = fields
          .get("labels")
          .map(_.convertTo[List[JsValue]])
          .getOrElse(List.empty[JsValue])
          .map(l => applyLabelsForExtractedData(l, LongId(0)))

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
