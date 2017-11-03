package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuicommon.logging._

final case class Message(id: LongId[Message],
                         text: String,
                         lastUpdate: LocalDateTime,
                         userId: xyz.driver.core.Id[User],
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

    val entityId: Option[String] = recordId
      .map(_.toString)
      .orElse(documentId.map(_.toString))
      .orElse(patientId.map(_.toString))

    phi"Message(id=$id, userId=${Unsafe(userId)}, isDraft=$isDraft, entityId=${Unsafe(entityId)}"
  }
}
