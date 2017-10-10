package xyz.driver.pdsuidomain.entities.export.trial

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.EligibilityArm

final case class ExportTrialArm(armId: LongId[EligibilityArm], armName: String)

object ExportTrialArm {

  implicit def toPhiString(x: ExportTrialArm): PhiString = {
    import x._
    phi"ExportTrialArm(armId=$armId, armName=${Unsafe(armName)})"
  }
}
