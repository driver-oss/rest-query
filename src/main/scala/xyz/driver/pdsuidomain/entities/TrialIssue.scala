package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.logging._

final case class TrialIssue(id: LongId[TrialIssue],
                            userId: xyz.driver.core.Id[User],
                            trialId: StringId[Trial],
                            lastUpdate: LocalDateTime,
                            isDraft: Boolean,
                            text: String,
                            evidence: String,
                            archiveRequired: Boolean,
                            meta: String)

object TrialIssue {
  implicit def toPhiString(x: TrialIssue): PhiString = {
    import x._
    phi"TrialIssue(id=$id, userId=${Unsafe(userId)}, trialId=$trialId)"
  }
}
