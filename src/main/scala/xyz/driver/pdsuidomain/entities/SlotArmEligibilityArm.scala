package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._

final case class SlotArmEligibilityArm(slotArmId: LongId[SlotArm], eligibilityArmId: LongId[EligibilityArm])

object SlotArmEligibilityArm {
  implicit def toPhiString(x: SlotArmEligibilityArm): PhiString = {
    import x._
    phi"SlotArmEligibilityArm(slotArmId=$slotArmId, eligibilityArmId=$eligibilityArmId)"
  }
}
