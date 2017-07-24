package xyz.driver.pdsuidomain.formats.json.document

import java.time.{LocalDate, ZoneId, ZonedDateTime}
import xyz.driver.pdsuicommon.domain.{LongId, StringId, TextJson}
import xyz.driver.pdsuicommon.json.JsonSerializer

import xyz.driver.pdsuidomain.entities._
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.json.JsonSerializer

final case class ApiDocument(id: Long,
                             recordId: Long,
                             physician: Option[String],
                             lastUpdate: ZonedDateTime,
                             typeId: Option[Long],
                             startDate: Option[LocalDate],
                             endDate: Option[LocalDate],
                             provider: Option[String],
                             providerTypeId: Option[Long],
                             requiredType: Option[String],
                             status: Option[String],
                             previousStatus: Option[String],
                             assignee: Option[String],
                             previousAssignee: Option[String],
                             lastActiveUser: Option[String],
                             meta: Option[String]) {

  private def extractStatus(status: String): Document.Status =
    Document.Status.fromString(status).getOrElse(throw new NoSuchElementException(s"Status $status unknown"))

  private def extractRequiredType(tpe: String): Document.RequiredType =
    Document.RequiredType.fromString(tpe).getOrElse(throw new NoSuchElementException(s"RequitedType $tpe unknown"))

  def toDomain = Document(
    id = LongId(this.id),
    status = extractStatus(this.status.getOrElse("")),
    previousStatus = previousStatus.map(extractStatus),
    assignee = this.assignee.map(StringId(_)),
    previousAssignee = this.previousAssignee.map(StringId(_)),
    lastActiveUserId = this.lastActiveUser.map(StringId(_)),
    recordId = LongId(this.recordId),
    physician = this.physician,
    typeId = this.typeId.map(LongId(_)),
    providerName = this.provider,
    providerTypeId = this.providerTypeId.map(LongId(_)),
    requiredType = this.requiredType.map(extractRequiredType),
    meta = this.meta.map(x => TextJson(JsonSerializer.deserialize[Document.Meta](x))),
    startDate = this.startDate,
    endDate = this.endDate,
    lastUpdate = this.lastUpdate.toLocalDateTime()
  )

}

object ApiDocument {

  private val statusFormat = Format(
    Reads.StringReads.filter(ValidationError("unknown status")) { x =>
      Document.Status.fromString(x).isDefined
    },
    Writes.StringWrites
  )

  implicit val format: Format[ApiDocument] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "recordId").format[Long] and
      (JsPath \ "physician").formatNullable[String] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "typeId").formatNullable[Long] and
      (JsPath \ "startDate").formatNullable[LocalDate] and
      (JsPath \ "endDate").formatNullable[LocalDate] and
      (JsPath \ "provider").formatNullable[String] and
      (JsPath \ "providerTypeId").formatNullable[Long] and
      (JsPath \ "requiredType").formatNullable[String] and
      (JsPath \ "status").formatNullable(statusFormat) and
      (JsPath \ "previousStatus").formatNullable(statusFormat) and
      (JsPath \ "assignee").formatNullable[String] and
      (JsPath \ "previousAssignee").formatNullable[String] and
      (JsPath \ "lastActiveUser").formatNullable[String] and
      (JsPath \ "meta").formatNullable(Format(Reads { x =>
        JsSuccess(Json.stringify(x))
      }, Writes[String](Json.parse)))
  )(ApiDocument.apply, unlift(ApiDocument.unapply))

  def fromDomain(document: Document): ApiDocument = {
    ApiDocument(
      id = document.id.id,
      recordId = document.recordId.id,
      physician = document.physician,
      lastUpdate = ZonedDateTime.of(document.lastUpdate, ZoneId.of("Z")),
      typeId = document.typeId.map(_.id),
      startDate = document.startDate,
      endDate = document.endDate,
      provider = document.providerName,
      providerTypeId = document.providerTypeId.map(_.id),
      requiredType = document.requiredType.map(Document.RequiredType.requiredTypeToString),
      status = Option(Document.Status.statusToString(document.status)),
      previousStatus = document.previousStatus.map(Document.Status.statusToString),
      assignee = document.assignee.map(_.id),
      previousAssignee = document.previousAssignee.map(_.id),
      lastActiveUser = document.lastActiveUserId.map(_.id),
      meta = document.meta.map(meta => JsonSerializer.serialize(meta.content))
    )
  }
}
