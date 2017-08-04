package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime
import java.util.UUID

import spray.json._
import xyz.driver.core.json.{EnumJsonFormat, GadtJsonFormat}
import xyz.driver.pdsuicommon.domain.{LongId, TextJson, UuidId}
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

  implicit val caseIdFormat = new RootJsonFormat[CaseId] {
    override def write(caseId: CaseId): JsString = JsString(caseId.toString)
    override def read(json: JsValue): CaseId = json match {
      case JsString(value) => CaseId(value)
      case _               => deserializationError(s"Expected string as CaseId, but got $json")
    }
  }

  implicit val recordMetaTypeFormat: GadtJsonFormat[MedicalRecord.Meta] = {
    import Meta._
    GadtJsonFormat.create[Meta]("meta")({ case m => m.metaType }) {
      case "duplicate" => jsonFormat5(Duplicate.apply)
      case "reorder"   => jsonFormat2(Reorder.apply)
      case "rotation"  => jsonFormat2(Rotation.apply)
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
          "id"               -> record.id.id.toJson,
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
          "lastUpdate"       -> record.lastUpdate.toJson
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
            predictedMeta = None,
            predictedDocuments = None,
            lastUpdate = LocalDateTime.now()
          )

        case _ => deserializationError(s"Expected Json Object as MedicalRecord, but got $json")
      }
    }

  def applyUpdateToMedicalRecord(json: JsValue, orig: MedicalRecord): MedicalRecord = json match {
    case JsObject(fields) =>
      val meta = if (fields.contains("meta")) {
        fields
          .get("meta")
          .map(_.convertTo[TextJson[List[Meta]]])
      } else orig.meta
      orig.copy(meta = meta)

    case _ => deserializationError(s"Expected Json Object as partial MedicalRecord, but got $json")
  }

}
