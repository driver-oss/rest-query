package xyz.driver.pdsuidomain.entities.export.trial

import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.Arm

case class ExportTrialArm(armId: LongId[Arm], armName: String)

object ExportTrialArm {

  implicit def toPhiString(x: ExportTrialArm): PhiString = {
    import x._
    phi"ExportTrialArm(armId=$armId, armName=${Unsafe(armName)})"
  }
}
