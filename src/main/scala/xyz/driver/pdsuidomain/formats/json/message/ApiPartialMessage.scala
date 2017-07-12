package xyz.driver.pdsuidomain.formats.json.message

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}
import xyz.driver.pdsuidomain.entities.Message

final case class ApiPartialMessage(text: Option[String],
                                   recordId: Option[Long],
                                   documentId: Option[Long],
                                   patientId: Option[String],
                                   trialId: Option[String],
                                   startPage: Option[Double],
                                   endPage: Option[Double],
                                   evidence: Option[String],
                                   archiveRequired: Option[Boolean],
                                   meta: Option[String]) {

  def toDomain(userId: StringId[User]) = Message(
    id = LongId(0),
    text = text.getOrElse(""),
    userId = userId,
    isDraft = true,
    recordId = recordId.map(LongId(_)),
    documentId = documentId.map(LongId(_)),
    patientId = patientId.map(UuidId(_)),
    trialId = trialId.map(StringId(_)),
    startPage = startPage,
    endPage = endPage,
    evidence = evidence,
    archiveRequired = archiveRequired,
    meta = meta,
    lastUpdate = LocalDateTime.MIN
  )

  def applyTo(orig: Message): Message = {
    orig.copy(
      text = text.getOrElse(""),
      recordId = recordId.map(LongId(_)),
      documentId = documentId.map(LongId(_)),
      patientId = patientId.map(UuidId(_)),
      trialId = trialId.map(StringId(_)),
      startPage = startPage,
      endPage = endPage,
      evidence = evidence,
      archiveRequired = archiveRequired,
      meta = meta,
      lastUpdate = LocalDateTime.MIN
    )
  }
}

object ApiPartialMessage {

  implicit val format: Format[ApiPartialMessage] = (
    (JsPath \ "text").formatNullable[String] and
      (JsPath \ "recordId").formatNullable[Long] and
      (JsPath \ "documentId").formatNullable[Long] and
      (JsPath \ "patientId").formatNullable[String] and
      (JsPath \ "trialId").formatNullable[String] and
      (JsPath \ "startPage").formatNullable[Double] and
      (JsPath \ "endPage").formatNullable[Double] and
      (JsPath \ "evidence").formatNullable[String] and
      (JsPath \ "archiveRequired").formatNullable[Boolean] and
      (JsPath \ "meta").formatNullable[String]
  )(ApiPartialMessage.apply, unlift(ApiPartialMessage.unapply))

  def fromDomain(domain: Message) = ApiPartialMessage(
    text = Some(domain.text),
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
}
