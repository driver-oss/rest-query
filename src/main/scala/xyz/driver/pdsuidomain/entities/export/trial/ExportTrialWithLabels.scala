package xyz.driver.pdsuidomain.entities.export.trial

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.Trial

final case class ExportTrialWithLabels(nctId: StringId[Trial],
                                       trialId: UuidId[Trial],
                                       lastReviewed: LocalDateTime,
                                       labelVersion: Long,
                                       arms: List[ExportTrialArm],
                                       criteria: List[ExportTrialLabelCriterion])

object ExportTrialWithLabels {

  implicit def toPhiString(x: ExportTrialWithLabels): PhiString = {
    import x._
    phi"TrialWithLabels(nctId=$nctId, trialId=$trialId}, " +
      phi"lastReviewed=$lastReviewed, labelVersion=${Unsafe(labelVersion)}, arms=$arms, criteria=$criteria)"
  }
}
