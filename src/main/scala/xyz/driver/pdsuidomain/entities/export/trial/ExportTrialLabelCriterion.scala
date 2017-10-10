package xyz.driver.pdsuidomain.entities.export.trial

import xyz.driver.entities.labels.Label
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{Criterion, EligibilityArm}

final case class ExportTrialLabelCriterion(criterionId: LongId[Criterion],
                                           value: Option[Boolean],
                                           labelId: LongId[Label],
                                           armIds: Set[LongId[EligibilityArm]],
                                           criteria: String,
                                           isCompound: Boolean,
                                           isDefining: Boolean)

object ExportTrialLabelCriterion {

  implicit def toPhiString(x: ExportTrialLabelCriterion): PhiString = {
    import x._
    phi"TrialLabelCriterion(criterionId=$criterionId, value=$value, labelId=$labelId, " +
      phi"criteria=${Unsafe(criteria)}, isCompound=$isCompound, isDefining=$isDefining)"
  }
}
