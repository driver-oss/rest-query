package xyz.driver.pdsuidomain.formats.json.record

import java.time.LocalDateTime
import java.util.UUID

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuidomain.entities._
import play.api.libs.json._

final case class ApiCreateRecord(disease: String, patientId: String, requestId: UUID, filename: String) {

  def toDomain = MedicalRecord(
    id = LongId(0),
    status = MedicalRecord.Status.New,
    previousStatus = None,
    assignee = None,
    previousAssignee = None,
    patientId = UuidId(patientId),
    requestId = RecordRequestId(requestId),
    disease = disease,
    caseId = None,
    physician = None,
    sourceName = filename,
    meta = None,
    predictedMeta = None,
    predictedDocuments = None,
    lastUpdate = LocalDateTime.now()
  )
}

object ApiCreateRecord {

  implicit val format: Format[ApiCreateRecord] = Json.format[ApiCreateRecord]
}
