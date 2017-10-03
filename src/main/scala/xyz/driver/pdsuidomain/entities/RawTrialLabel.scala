package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.entities.labels.Label
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuicommon.logging._

final case class RawTrialLabel(nctId: StringId[Trial],
                               trialId: UuidId[Trial],
                               condition: String,
                               lastReviewed: LocalDateTime,
                               armName: String,
                               armId: LongId[Arm],
                               labelId: LongId[Label],
                               value: Option[Boolean],
                               criterionId: LongId[Criterion],
                               criteria: String,
                               criterionArmId: LongId[Arm],
                               isCompound: Boolean,
                               isDefining: Boolean)

object RawTrialLabel {

  implicit def toPhiString(x: RawTrialLabel): PhiString = {
    import x._
    phi"RawTrialLabel(nctId=$nctId, trialId=$trialId, condition=${Unsafe(condition)}, lastReviewed=$lastReviewed, " +
      phi"armId=$armId, armName=${Unsafe(armName)}, labelId=$labelId, value=$value, " +
      phi"criterionId=$criterionId, criteria=${Unsafe(criteria)}, criterionArmId=$criterionArmId, " +
      phi"isCompound=$isCompound, isDefining=$isDefining)"
  }
}
