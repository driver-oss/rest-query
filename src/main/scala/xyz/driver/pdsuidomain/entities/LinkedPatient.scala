package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._

object LinkedPatient {

  implicit def toPhiString(x: LinkedPatient): PhiString = {
    import x._
    phi"LinkedPatient(userId=$userId, patientId=$patientId, trialId=$trialId)"
  }
}

case class LinkedPatient(userId: StringId[User], patientId: UuidId[Patient], trialId: StringId[Trial])
