package xyz.driver.pdsuidomain.formats.json.record

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import xyz.driver.pdsuidomain.entities.CaseId
import xyz.driver.pdsuidomain.entities.MedicalRecord
import xyz.driver.pdsuidomain.entities.MedicalRecord.Status
import xyz.driver.pdsuidomain.entities.RecordRequestId
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.json.JsonSerializer
import xyz.driver.pdsuicommon.domain.{LongId, StringId, TextJson, UuidId}

object ApiRecord {

  private val emptyMeta: String =
    "[]"

  private val statusFormat = Format(
    Reads.StringReads.filter(ValidationError("unknown status")) {
      case x if MedicalRecordStatus.statusFromString.isDefinedAt(x) => true
      case _                                                        => false
    },
    Writes.StringWrites
  )

  implicit val format: Format[ApiRecord] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "patientId").format[String] and
      (JsPath \ "caseId").formatNullable[String] and
      (JsPath \ "disease").format[String] and
      (JsPath \ "physician").formatNullable[String] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "status").format(statusFormat) and
      (JsPath \ "previousStatus").formatNullable(statusFormat) and
      (JsPath \ "assignee").formatNullable[String] and
      (JsPath \ "previousAssignee").formatNullable[String] and
      (JsPath \ "lastActiveUser").formatNullable[String] and
      (JsPath \ "requestId").format[UUID] and
      (JsPath \ "meta").format(Format(Reads { x =>
        JsSuccess(Json.stringify(x))
      }, Writes[String](Json.parse)))
    )(ApiRecord.apply, unlift(ApiRecord.unapply))

  def fromDomain(record: MedicalRecord) = ApiRecord(
    id = record.id.id,
    patientId = record.patientId.toString,
    caseId = record.caseId.map(_.id),
    disease = record.disease,
    physician = record.physician,
    lastUpdate = ZonedDateTime.of(record.lastUpdate, ZoneId.of("Z")),
    status = MedicalRecordStatus.statusToString(record.status),
    previousStatus = record.previousStatus.map(MedicalRecordStatus.statusToString),
    assignee = record.assignee.map(_.id),
    previousAssignee = record.previousAssignee.map(_.id),
    lastActiveUser = record.lastActiveUserId.map(_.id),
    requestId = record.requestId.id,
    meta = record.meta.map(x => JsonSerializer.serialize(x.content)).getOrElse(emptyMeta)
  )
}

final case class ApiRecord(id: Long,
                           patientId: String,
                           caseId: Option[String],
                           disease: String,
                           physician: Option[String],
                           lastUpdate: ZonedDateTime,
                           status: String,
                           previousStatus: Option[String],
                           assignee: Option[String],
                           previousAssignee: Option[String],
                           lastActiveUser: Option[String],
                           requestId: UUID,
                           meta: String) {

  private def extractStatus(status: String): Status =
    Status
      .fromString(status)
      .getOrElse(
        throw new NoSuchElementException(s"Status $status not found")
      )

  def toDomain = MedicalRecord(
    id = LongId(this.id),
    status = extractStatus(this.status),
    previousStatus = this.previousStatus.map(extractStatus),
    assignee = this.assignee.map(StringId(_)),
    previousAssignee = this.previousAssignee.map(StringId(_)),
    lastActiveUserId = this.lastActiveUser.map(StringId(_)),
    patientId = UuidId(patientId),
    requestId = RecordRequestId(this.requestId),
    disease = this.disease,
    caseId = caseId.map(CaseId(_)),
    physician = this.physician,
    meta = {
      if (this.meta == ApiRecord.emptyMeta) {
        None
      } else {
        Some(TextJson(JsonSerializer.deserialize[List[MedicalRecord.Meta]](this.meta)))
      }
    },
    predictedMeta = None,
    predictedDocuments = None,
    lastUpdate = this.lastUpdate.toLocalDateTime()
  )
}