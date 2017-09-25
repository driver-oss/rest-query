package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.logging._

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
