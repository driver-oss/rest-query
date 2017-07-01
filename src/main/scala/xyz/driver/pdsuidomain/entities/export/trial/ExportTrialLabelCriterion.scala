package xyz.driver.pdsuidomain.entities.export.trial

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{Arm, Criterion, Label, RawTrialLabel}

final case class ExportTrialLabelCriterion(criterionId: LongId[Criterion],
                                           value: Option[Boolean],
                                           labelId: LongId[Label],
                                           armIds: Set[LongId[Arm]],
                                           criteria: String,
                                           isCompound: Boolean,
                                           isDefining: Boolean)

object ExportTrialLabelCriterion {

  implicit def toPhiString(x: ExportTrialLabelCriterion): PhiString = {
    import x._
    phi"TrialLabelCriterion(criterionId=$criterionId, value=$value, labelId=$labelId, " +
      phi"criteria=${Unsafe(criteria)}, isCompound=$isCompound, isDefining=$isDefining)"
  }

  def fromRaw(x: RawTrialLabel, armIds: Set[LongId[Arm]]) = ExportTrialLabelCriterion(
    criterionId = x.criterionId,
    value = x.value,
    labelId = x.labelId,
    armIds = armIds,
    criteria = x.criteria,
    isCompound = x.isCompound,
    isDefining = x.isDefining
  )
}
