package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, StringId, UuidId}
import xyz.driver.pdsuicommon.logging._

object PatientTrialArm {

  implicit def toPhiString(x: PatientTrialArm): PhiString = {
    import x._
    phi"PatientTrialArm(armId=$armId, trialArmGroupId=$trialArmGroupId)"
  }
}

final case class PatientTrialArm(armId: LongId[Arm], trialArmGroupId: LongId[PatientTrialArmGroup])

object PatientEligibleTrial {
  implicit def toPhiString(x: PatientEligibleTrial): PhiString = {
    import x._
    phi"PatientEligibleTrial(id=$id, patientId=$patientId, trialId=$trialId, hypothesisId=$hypothesisId)"
  }
}

final case class PatientEligibleTrial(id: UuidId[PatientEligibleTrial],
                                      patientId: UuidId[Patient],
                                      trialId: StringId[Trial],
                                      hypothesisId: UuidId[Hypothesis])

object PatientTrialArmGroup {

  implicit def toPhiString(x: PatientTrialArmGroup): PhiString = {
    import x._
    phi"PatientTrialArmGroup(id=$id, eligibleTrialId=$eligibleTrialId, " +
      phi"eligibilityStatus=${Unsafe(eligibilityStatus)}, " +
      phi"verifiedEligibilityStatus=${Unsafe(verifiedEligibilityStatus)}, isVerified=$isVerified)"
  }

  /**
    * @see https://driverinc.atlassian.net/wiki/display/DMPD/EV+Business+Process
    */
  def getEligibilityStatusForRc(criterionList: TraversableOnce[PatientCriterion]): Option[FuzzyValue] = {
    def isEligible: Boolean   = criterionList.forall(_.verifiedEligibilityStatus.contains(FuzzyValue.Yes))
    def isIneligible: Boolean = criterionList.exists(_.verifiedEligibilityStatus.contains(FuzzyValue.No))
    def isUnknown: Boolean    = criterionList.forall(_.verifiedEligibilityStatus.isEmpty)

    if (isEligible) Some(FuzzyValue.Yes)
    else if (isIneligible) Some(FuzzyValue.No)
    else if (isUnknown) None
    else Some(FuzzyValue.Maybe)
  }
}

final case class PatientTrialArmGroup(id: LongId[PatientTrialArmGroup],
                                      eligibleTrialId: UuidId[PatientEligibleTrial],
                                      eligibilityStatus: Option[FuzzyValue],
                                      verifiedEligibilityStatus: Option[FuzzyValue],
                                      isVerified: Boolean)

object PatientTrialArmGroupView {

  implicit def toPhiString(x: PatientTrialArmGroupView): PhiString = {
    import x._
    phi"PatientTrialArmGroupView(id=$id, patientId=$patientId, trialId=$trialId, " +
      phi"hypothesisId=$hypothesisId, eligibilityStatus=${Unsafe(eligibilityStatus)}, " +
      phi"verifiedEligibilityStatus=${Unsafe(verifiedEligibilityStatus)}, isVerified=$isVerified)"
  }
}

final case class PatientTrialArmGroupView(id: LongId[PatientTrialArmGroup],
                                          patientId: UuidId[Patient],
                                          trialId: StringId[Trial],
                                          hypothesisId: UuidId[Hypothesis],
                                          eligibilityStatus: Option[FuzzyValue],
                                          verifiedEligibilityStatus: Option[FuzzyValue],
                                          isVerified: Boolean) {

  def applyTo(trialArmGroup: PatientTrialArmGroup) = {
    trialArmGroup.copy(
      eligibilityStatus = this.eligibilityStatus,
      verifiedEligibilityStatus = this.verifiedEligibilityStatus,
      isVerified = this.isVerified
    )
  }
}
