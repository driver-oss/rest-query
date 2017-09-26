package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.logging._

final case class InterventionType(id: LongId[InterventionType], name: String)

object InterventionType {
  implicit def toPhiString(x: InterventionType): PhiString = {
    import x._
    phi"InterventionType(id=$id, name=${Unsafe(name)})"
  }
}

final case class InterventionArm(armId: LongId[Arm], interventionId: LongId[Intervention])

object InterventionArm {
  implicit def toPhiString(x: InterventionArm): PhiString = {
    import x._
    phi"InterventionArm(armId=$armId, interventionId=$interventionId)"
  }
}

final case class Intervention(id: LongId[Intervention],
                              trialId: StringId[Trial],
                              name: String,
                              originalName: String,
                              typeId: Option[LongId[InterventionType]],
                              originalType: Option[String],
                              dosage: String,
                              originalDosage: String,
                              isActive: Boolean,
                              deliveryMethod: Option[String])

object Intervention {
  implicit def toPhiString(x: Intervention): PhiString = {
    import x._
    phi"Intervention(id=$id, trialId=$trialId, name=${Unsafe(name)}, typeId=$typeId, isActive=$isActive, deliveryMethod=${Unsafe(deliveryMethod)})"
  }
}

final case class InterventionWithArms(intervention: Intervention, arms: List[InterventionArm])

object InterventionWithArms {
  implicit def toPhiString(x: InterventionWithArms): PhiString = {
    import x._
    phi"InterventionWithArms(intervention=$intervention, arms=$arms)"
  }
}
