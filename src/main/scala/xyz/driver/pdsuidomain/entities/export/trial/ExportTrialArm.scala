package xyz.driver.pdsuidomain.entities.export.trial

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.Arm

final case class ExportTrialArm(armId: LongId[Arm], armName: String, diseaseList: Seq[String])

object ExportTrialArm {

  implicit def toPhiString(x: ExportTrialArm): PhiString = {
    import x._
    phi"ExportTrialArm(armId=$armId, armName=${Unsafe(armName)})"
  }
}
