package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime
import java.util.UUID

import spray.json._
import xyz.driver.core.json.{EnumJsonFormat, GadtJsonFormat}
import xyz.driver.pdsuicommon.domain.{LongId, TextJson, UuidId}
import xyz.driver.pdsuidomain.entities.MedicalRecord.Meta._
import xyz.driver.pdsuidomain.entities._

object record {
  import DefaultJsonProtocol._
  import MedicalRecord._
  import common._

  implicit val recordStatusFormat = new EnumJsonFormat[Status](
    "Unprocessed"   -> Status.Unprocessed,
    "PreOrganized"  -> Status.PreOrganized,
    "New"           -> Status.New,
    "Cleaned"       -> Status.Cleaned,
    "PreOrganized"  -> Status.PreOrganized,
    "PreOrganizing" -> Status.PreOrganizing,
    "Reviewed"      -> Status.Reviewed,
    "Organized"     -> Status.Organized,
    "Done"          -> Status.Done,
    "Flagged"       -> Status.Flagged,
    "Archived"      -> Status.Archived
  )

  implicit val requestIdFormat = new RootJsonFormat[RecordRequestId] {
    override def write(requestId: RecordRequestId): JsString = JsString(requestId.toString)
    override def read(json: JsValue): RecordRequestId = json match {
      case JsString(value) => RecordRequestId(UUID.fromString(value))
      case _               => deserializationError(s"Expected string as RecordRequestId, but got $json")
    }
  }

  implicit val providerTypeFormat: RootJsonFormat[ProviderType] = new RootJsonFormat[ProviderType] {
    override def read(json: JsValue): ProviderType = json match {
      case JsObject(fields) =>
        val name = fields
          .get("name")
          .map(_.convertTo[String])
          .getOrElse(deserializationError(s"Provider type json object does not contain `name` field: $json"))

        ProviderType
          .fromString(name)
          .getOrElse(deserializationError(s"Unknown provider type: $name"))

      case _ => deserializationError(s"Expected Json Object as Provider type, but got $json")
    }

    override def write(obj: ProviderType) =
      JsObject("id" -> obj.id.toJson, "name" -> obj.name.toJson)
  }

  implicit val caseIdFormat = new RootJsonFormat[CaseId] {
    override def write(caseId: CaseId): JsString = JsString(caseId.toString)
    override def read(json: JsValue): CaseId = json match {
      case JsString(value) => CaseId(value)
      case _               => deserializationError(s"Expected string as CaseId, but got $json")
    }
  }

  implicit val duplicateMetaFormat: RootJsonFormat[Duplicate] = new RootJsonFormat[Duplicate] {
    override def write(obj: Duplicate) =
      JsObject(
        "type"              -> "duplicate".toJson,
        "startPage"         -> obj.startPage.toJson,
        "endPage"           -> obj.endPage.toJson,
        "startOriginalPage" -> obj.startOriginalPage.toJson,
        "endOriginalPage"   -> obj.endOriginalPage.toJson
      )

    override def read(json: JsValue): Duplicate = json match {
      case JsObject(fields) =>
        val startPage = fields
          .get("startPage")
          .map(_.convertTo[Double])
          .getOrElse(deserializationError(s"Duplicate meta json object does not contain `startPage` field: $json"))

        val endPage = fields
          .get("endPage")
          .map(_.convertTo[Double])
          .getOrElse(deserializationError(s"Duplicate meta json object does not contain `endPage` field: $json"))

        val startOriginalPage = fields
          .get("startOriginalPage")
          .map(_.convertTo[Double])
          .getOrElse(
            deserializationError(s"Duplicate meta json object does not contain `startOriginalPage` field: $json"))

        val endOriginalPage = fields
          .get("endOriginalPage")
          .map(_.convertTo[Double])

        Duplicate(
          startPage = startPage,
          endPage = endPage,
          startOriginalPage = startOriginalPage,
          endOriginalPage = endOriginalPage
        )

      case _ => deserializationError(s"Expected JsObject as Duplicate meta of medical record, but got $json")
    }
  }

  implicit val reorderMetaFormat: RootJsonFormat[Reorder] = new RootJsonFormat[Reorder] {
    override def write(obj: Reorder) =
      JsObject("type" -> "reorder".toJson, "items" -> obj.items.toJson)

    override def read(json: JsValue): Reorder = json match {
      case JsObject(fields) =>
        val items = fields
          .get("items")
          .map(_.convertTo[Seq[Int]])
          .getOrElse(deserializationError(s"Reorder meta json object does not contain `items` field: $json"))

        Reorder(items)

      case _ => deserializationError(s"Expected JsObject as Reorder meta of medical record, but got $json")
    }
  }

  implicit val rotateMetaFormat: RootJsonFormat[Rotation] = new RootJsonFormat[Rotation] {
    override def write(obj: Rotation) =
      JsObject("type" -> "rotation".toJson, "items" -> obj.items.toJson)

    override def read(json: JsValue): Rotation = json match {
      case JsObject(fields) =>
        val items = fields
          .get("items")
          .map(_.convertTo[Map[String, Int]])
          .getOrElse(deserializationError(s"Rotation meta json object does not contain `items` field: $json"))

        Rotation(items = items)

      case _ => deserializationError(s"Expected JsObject as Rotation meta of medical record, but got $json")
    }
  }

  implicit val recordMetaTypeFormat: GadtJsonFormat[MedicalRecord.Meta] = {
    GadtJsonFormat.create[Meta]("type")({ case m => m.metaType }) {
      case "duplicate" => duplicateMetaFormat
      case "reorder"   => reorderMetaFormat
      case "rotation"  => rotateMetaFormat
    }
  }

  implicit val recordMetaFormat = new RootJsonFormat[TextJson[List[Meta]]] {
    override def write(obj: TextJson[List[Meta]]): JsArray = JsArray(obj.content.map(_.toJson).toVector)
    override def read(json: JsValue): TextJson[List[Meta]] = json match {
      case JsArray(values) => TextJson[List[Meta]](values.map(_.convertTo[Meta]).toList)
      case _               => deserializationError(s"Expected array as Meta, but got $json")
    }
  }

  implicit val recordFormat: RootJsonFormat[MedicalRecord] =
    new RootJsonFormat[MedicalRecord] {
      override def write(record: MedicalRecord): JsValue =
        JsObject(
          "id"               -> record.id.toJson,
          "patientId"        -> record.patientId.toJson,
          "caseId"           -> record.caseId.toJson,
          "disease"          -> record.disease.toJson,
          "physician"        -> record.physician.toJson,
          "status"           -> record.status.toJson,
          "previousStatus"   -> record.previousStatus.toJson,
          "assignee"         -> record.assignee.toJson,
          "previousAssignee" -> record.previousAssignee.toJson,
          "requestId"        -> record.requestId.toJson,
          "meta"             -> record.meta.getOrElse(TextJson[List[Meta]](List.empty)).toJson,
          "lastActiveUser"   -> record.lastActiveUserId.toJson,
          "lastUpdate"       -> record.lastUpdate.toJson,
          "totalPages"       -> record.totalPages.toJson
        )

      override def read(json: JsValue): MedicalRecord = json match {
        case JsObject(fields) =>
          val disease = fields
            .get("disease")
            .map(_.convertTo[String])
            .getOrElse(deserializationError(s"MedicalRecord json object does not contain `disease` field: $json"))

          val patientId = fields
            .get("patientId")
            .map(_.convertTo[UuidId[Patient]])
            .getOrElse(deserializationError(s"MedicalRecord json object does not contain `patientId` field: $json"))

          val requestId = fields
            .get("requestId")
            .map(_.convertTo[RecordRequestId])
            .getOrElse(deserializationError(s"MedicalRecord json object does not contain `requestId` field: $json"))

          MedicalRecord(
            id = LongId(0),
            status = MedicalRecord.Status.New,
            previousStatus = None,
            assignee = None,
            previousAssignee = None,
            lastActiveUserId = None,
            patientId = patientId,
            requestId = requestId,
            disease = disease,
            caseId = None,
            physician = None,
            meta = None,
            lastUpdate = LocalDateTime.now(),
            totalPages = 0
          )

        case _ => deserializationError(s"Expected Json Object as MedicalRecord, but got $json")
      }
    }

  def applyUpdateToMedicalRecord(json: JsValue, orig: MedicalRecord): MedicalRecord = json match {
    case JsObject(fields) =>
      val meta = fields
        .get("meta")
        .map(_.convertTo[Option[TextJson[List[Meta]]]])
        .getOrElse(orig.meta)
      orig.copy(meta = meta)

    case _ => deserializationError(s"Expected Json Object as partial MedicalRecord, but got $json")
  }

}
