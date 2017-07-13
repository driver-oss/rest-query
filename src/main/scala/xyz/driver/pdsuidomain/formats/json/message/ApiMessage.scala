package xyz.driver.pdsuidomain.formats.json.message

import java.time.{ZoneId, ZonedDateTime}

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.Message

final case class ApiMessage(id: Long,
                            text: String,
                            lastUpdate: ZonedDateTime,
                            userId: String,
                            isDraft: Boolean,
                            recordId: Option[Long],
                            documentId: Option[Long],
                            patientId: Option[String],
                            trialId: Option[String],
                            startPage: Option[Double],
                            endPage: Option[Double],
                            evidence: Option[String],
                            archiveRequired: Option[Boolean],
                            meta: Option[String])

object ApiMessage {

  def fromDomain(domain: Message) = ApiMessage(
    id = domain.id.id,
    text = domain.text,
    lastUpdate = ZonedDateTime.of(domain.lastUpdate, ZoneId.of("Z")),
    userId = domain.userId.id,
    isDraft = domain.isDraft,
    recordId = domain.recordId.map(_.id),
    documentId = domain.documentId.map(_.id),
    patientId = domain.patientId.map(_.toString),
    trialId = domain.trialId.map(_.toString),
    startPage = domain.startPage,
    endPage = domain.endPage,
    evidence = domain.evidence,
    archiveRequired = domain.archiveRequired,
    meta = domain.meta
  )

  implicit val format: Format[ApiMessage] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "text").format[String] and
      (JsPath \ "lastUpdate").format[ZonedDateTime] and
      (JsPath \ "userId").format[String] and
      (JsPath \ "isDraft").format[Boolean] and
      (JsPath \ "recordId").formatNullable[Long] and
      (JsPath \ "documentId").formatNullable[Long] and
      (JsPath \ "patientId").formatNullable[String] and
      (JsPath \ "trialId").formatNullable[String] and
      (JsPath \ "startPage").formatNullable[Double] and
      (JsPath \ "endPage").formatNullable[Double] and
      (JsPath \ "evidence").formatNullable[String] and
      (JsPath \ "archiveRequired").formatNullable[Boolean] and
      (JsPath \ "meta").formatNullable[String]
  )(ApiMessage.apply, unlift(ApiMessage.unapply))
}
