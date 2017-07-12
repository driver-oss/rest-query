package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain.{LongId, StringId, User, UuidId}
import xyz.driver.pdsuicommon.logging._

final case class Message(id: LongId[Message],
                         text: String,
                         lastUpdate: LocalDateTime,
                         userId: StringId[User],
                         isDraft: Boolean,
                         recordId: Option[LongId[MedicalRecord]],
                         documentId: Option[LongId[Document]],
                         patientId: Option[UuidId[Patient]],
                         trialId: Option[StringId[Trial]],
                         startPage: Option[Double],
                         endPage: Option[Double],
                         evidence: Option[String],
                         archiveRequired: Option[Boolean],
                         meta: Option[String])

object Message {
  implicit def toPhiString(x: Message): PhiString = {
    import x._

    val entityId = recordId
      .orElse(documentId)
      .orElse(patientId)
      .map(_.toString)

    phi"Message(id=$id, userId=$userId, isDraft=$isDraft, entityId=${Unsafe(entityId)}"
  }
}
