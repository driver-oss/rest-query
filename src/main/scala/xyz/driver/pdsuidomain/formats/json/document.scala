package xyz.driver.pdsuidomain.formats.json

import java.time.{LocalDate, LocalDateTime}

import spray.json._
import xyz.driver.core.json._
import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuidomain.entities._

object document {
  import DefaultJsonProtocol._
  import Document._
  import common._

  implicit val documentStatusFormat: RootJsonFormat[Status] = new EnumJsonFormat[Status](
    "New"       -> Status.New,
    "Organized" -> Status.Organized,
    "Extracted" -> Status.Extracted,
    "Done"      -> Status.Done,
    "Flagged"   -> Status.Flagged,
    "Archived"  -> Status.Archived
  )

  implicit val requiredTypeFormat: RootJsonFormat[RequiredType] = new EnumJsonFormat[RequiredType](
    "OPN" -> RequiredType.OPN,
    "PN"  -> RequiredType.PN
  )

  implicit val documentMetaFormat: RootJsonFormat[Meta] = jsonFormat2(Meta.apply)

  implicit val documentTypeFormat: RootJsonFormat[DocumentType] = new RootJsonFormat[DocumentType] {
    override def read(json: JsValue): DocumentType = json match {
      case JsObject(fields) =>
        val name = fields
          .get("name")
          .map(_.convertTo[String])
          .getOrElse(deserializationError(s"Document type json object does not contain `name` field: $json"))

        DocumentType
          .fromString(name)
          .getOrElse(deserializationError(s"Unknown document type: $name"))

      case _ => deserializationError(s"Expected Json Object as Document type, but got $json")
    }

    override def write(obj: DocumentType) =
      JsObject("id" -> obj.id.toJson, "name" -> obj.name.toJson)
  }

  implicit val fullDocumentMetaFormat = new RootJsonFormat[TextJson[Meta]] {
    override def write(obj: TextJson[Meta]): JsValue = obj.content.toJson
    override def read(json: JsValue)                 = TextJson(documentMetaFormat.read(json))
  }

  def applyUpdateToDocument(json: JsValue, orig: Document): Document = json match {
    case JsObject(fields) =>
      val physician = fields
        .get("physician")
        .map(_.convertTo[String])

      val typeId = fields
        .get("typeId")
        .map(_.convertTo[Option[LongId[DocumentType]]])
        .getOrElse(orig.typeId)

      val provider = fields
        .get("provider")
        .map(_.convertTo[Option[String]])
        .getOrElse(orig.providerName)

      val providerTypeId = fields
        .get("providerTypeId")
        .map(_.convertTo[Option[LongId[ProviderType]]])
        .getOrElse(orig.providerTypeId)

      val institutionName = fields
        .get("institutionName")
        .map(_.convertTo[Option[String]])
        .getOrElse(orig.institutionName)

      val meta = fields
        .get("meta")
        .map(_.convertTo[Option[TextJson[Meta]]])
        .getOrElse(orig.meta)

      val startDate = fields
        .get("startDate")
        .map(_.convertTo[Option[LocalDate]])
        .getOrElse(orig.startDate)

      val endDate = fields
        .get("endDate")
        .map(_.convertTo[Option[LocalDate]])
        .getOrElse(orig.endDate)

      orig.copy(
        physician = physician.orElse(orig.physician),
        typeId = typeId,
        providerName = provider,
        providerTypeId = providerTypeId,
        institutionName = institutionName,
        meta = meta,
        startDate = startDate,
        endDate = endDate
      )

    case _ => deserializationError(s"Expected Json Object as partial Document, but got $json")
  }

  implicit val documentFormat: RootJsonFormat[Document] = new RootJsonFormat[Document] {
    override def write(document: Document): JsValue =
      JsObject(
        "id"               -> document.id.toJson,
        "recordId"         -> document.recordId.toJson,
        "physician"        -> document.physician.toJson,
        "typeId"           -> document.typeId.toJson,
        "provider"         -> document.providerName.toJson,
        "providerTypeId"   -> document.providerTypeId.toJson,
        "requiredType"     -> document.requiredType.toJson,
        "institutionName"  -> document.institutionName.toJson,
        "startDate"        -> document.startDate.toJson,
        "endDate"          -> document.endDate.toJson,
        "status"           -> document.status.toJson,
        "previousStatus"   -> document.previousStatus.toJson,
        "assignee"         -> document.assignee.toJson,
        "previousAssignee" -> document.previousAssignee.toJson,
        "meta"             -> document.meta.toJson,
        "lastActiveUser"   -> document.lastActiveUserId.toJson,
        "lastUpdate"       -> document.lastUpdate.toJson,
        "labelVersion"     -> document.labelVersion.toJson
      )

    override def read(json: JsValue): Document = json match {
      case JsObject(fields) =>
        val id = fields
          .get("id")
          .flatMap(_.convertTo[Option[LongId[Document]]])

        val recordId = fields
          .get("recordId")
          .map(_.convertTo[LongId[MedicalRecord]])
          .getOrElse(deserializationError(s"Document create json object does not contain `recordId` field: $json"))

        val physician = fields
          .get("physician")
          .flatMap(_.convertTo[Option[String]])

        val typeId = fields
          .get("typeId")
          .flatMap(_.convertTo[Option[LongId[DocumentType]]])

        val provider = fields
          .get("provider")
          .flatMap(_.convertTo[Option[String]])

        val providerTypeId = fields
          .get("providerTypeId")
          .flatMap(_.convertTo[Option[LongId[ProviderType]]])

        val institutionName = fields
          .get("institutionName")
          .flatMap(_.convertTo[Option[String]])

        val meta = fields
          .get("meta")
          .flatMap(_.convertTo[Option[TextJson[Meta]]])

        val startDate = fields
          .get("startDate")
          .flatMap(_.convertTo[Option[LocalDate]])

        val endDate = fields
          .get("endDate")
          .flatMap(_.convertTo[Option[LocalDate]])

        Document(
          id = id.getOrElse(LongId(0)),
          recordId = recordId,
          status = Document.Status.New,
          physician = physician,
          typeId = typeId,
          startDate = startDate,
          endDate = endDate,
          providerName = provider,
          providerTypeId = providerTypeId,
          requiredType = None,
          institutionName = institutionName,
          meta = meta,
          previousStatus = None,
          assignee = None,
          previousAssignee = None,
          lastActiveUserId = None,
          lastUpdate = LocalDateTime.MIN,
          labelVersion = 0
        )

      case _ => deserializationError(s"Expected Json Object as Document, but got $json")
    }
  }

}
