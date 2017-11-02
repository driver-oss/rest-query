package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.logging._

final case class MedicalRecordIssue(id: LongId[MedicalRecordIssue],
                                    userId: StringId[User],
                                    recordId: LongId[MedicalRecord],
                                    startPage: Option[Double],
                                    endPage: Option[Double],
                                    lastUpdate: LocalDateTime,
                                    isDraft: Boolean,
                                    text: String,
                                    archiveRequired: Boolean)

object MedicalRecordIssue {
  implicit def toPhiString(x: MedicalRecordIssue): PhiString = {
    import x._
    phi"MedicalRecordIssue(id=$id, userId=$userId, recordId=$recordId)"
  }
}
