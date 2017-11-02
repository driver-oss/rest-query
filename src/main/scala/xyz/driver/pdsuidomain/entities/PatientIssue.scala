package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._

final case class PatientIssue(id: LongId[PatientIssue],
                              userId: StringId[User],
                              patientId: UuidId[Patient],
                              lastUpdate: LocalDateTime,
                              isDraft: Boolean,
                              text: String,
                              archiveRequired: Boolean)

object PatientIssue {
  implicit def toPhiString(x: PatientIssue): PhiString = {
    import x._
    phi"PatientIssue(id=$id, userId=$userId, patientId=$patientId)"
  }
}
