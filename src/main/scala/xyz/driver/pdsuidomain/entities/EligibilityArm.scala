package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.Trial.Condition

final case class EligibilityArm(id: LongId[EligibilityArm],
                                name: String,
                                originalName: String,
                                trialId: StringId[Trial],
                                deleted: Option[LocalDateTime] = None)

object EligibilityArm {

  implicit def toPhiString(x: EligibilityArm): PhiString = {
    import x._
    phi"Arm(id=$id, name=${Unsafe(x.name)}, trialId=${Unsafe(x.trialId)})"
  }
}

final case class EligibilityArmDisease(eligibilityArmId: LongId[EligibilityArm],
                                       disease: Condition)

object EligibilityArmDisease {

  implicit def toPhiString(x: EligibilityArmDisease): PhiString = {
    phi"EligibilityArmDisease(eligibilityArmId=${Unsafe(x.eligibilityArmId)}, disease=${Unsafe(x.disease)})"
  }
}
