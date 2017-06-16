package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, StringId, UuidId}
import xyz.driver.pdsuicommon.logging._

object PatientCriterion {
  implicit def toPhiString(x: PatientCriterion): PhiString = {
    import x._
    phi"PatientCriterion(id=$id, patientLabelId=$patientLabelId, trialId=${Unsafe(trialId)}, nctId=${Unsafe(nctId)}, " +
      phi"criterionId=$criterionId, criterionValue=${Unsafe(criterionValue)}, " +
      phi"isImplicitMatch=$criterionIsDefining), criterionIsDefining=${Unsafe(criterionIsDefining)}, " +
      phi"eligibilityStatus=${Unsafe(eligibilityStatus)}, verifiedEligibilityStatus=${Unsafe(verifiedEligibilityStatus)}, " +
      phi"isVerified=${Unsafe(isVerified)}, lastUpdate=${Unsafe(lastUpdate)}"
  }

  /**
    * @see https://driverinc.atlassian.net/wiki/display/MTCH/EV+Business+Process
    */
  def getEligibilityStatus(criterionValue: Option[Boolean], primaryValue: Option[FuzzyValue]): Option[FuzzyValue] = {
    primaryValue match {
      case None                              => None
      case Some(FuzzyValue.Maybe)            => Some(FuzzyValue.Maybe)
      case Some(_) if criterionValue.isEmpty => Some(FuzzyValue.Maybe)
      case Some(status) =>
        Some(
          FuzzyValue.fromBoolean(
            FuzzyValue.fromBoolean(criterionValue.get) == status
          ))
    }
  }

}

/**
  * @param eligibilityStatus - a value, that selects an eligibility verifier (EV)
  * @param verifiedEligibilityStatus - a copy of eligibilityStatus, when a patient goes to routes curator (RC)
  * @param isVerified - is EV selected the eligibilityStatus?
  */
final case class PatientCriterion(id: LongId[PatientCriterion],
                                  patientLabelId: LongId[PatientLabel],
                                  trialId: Long,
                                  nctId: StringId[Trial],
                                  criterionId: LongId[Criterion],
                                  criterionText: String,
                                  criterionValue: Option[Boolean],
                                  criterionIsDefining: Boolean,
                                  eligibilityStatus: Option[FuzzyValue],
                                  verifiedEligibilityStatus: Option[FuzzyValue],
                                  isVerified: Boolean,
                                  isVisible: Boolean,
                                  lastUpdate: LocalDateTime) {
  def isIneligible: Boolean = eligibilityStatus.contains(FuzzyValue.No) && isVerified
}

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
      phi"eligibilityStatus=${Unsafe(eligibilityStatus)}, isVerified=$isVerified)"
  }

  /**
    * @see https://driverinc.atlassian.net/wiki/display/DMPD/EV+Business+Process
    */
  def getEligibilityStatusForEvToRc(criterionList: List[(Option[Boolean], Option[FuzzyValue])]): Option[FuzzyValue] = {
    def isEligible = {
      // Eligible, if for all (verified for EV) label-criteria eligibilityStatus=NULL or YES and at least one has status=YES
      // If method executes from PatientService (when EV submit patient) need to check value PatientCriterion.isVerified
      val verifiedList = criterionList.filter { case (isVerifiedOpt, _) => isVerifiedOpt.isEmpty || isVerifiedOpt.get }
      verifiedList.forall {
        case (_, eligibilityStatus) =>
          eligibilityStatus.isEmpty || eligibilityStatus.contains(FuzzyValue.Yes)
      } && verifiedList.exists { case (_, eligibilityStatus) => eligibilityStatus.contains(FuzzyValue.Yes) }
    }

    if (criterionList.exists {
          case (isVerified, eligibilityStatus) =>
            eligibilityStatus.contains(FuzzyValue.No) && (isVerified.isEmpty || isVerified.get)
        }) { Some(FuzzyValue.No) } else if (criterionList.forall {
                                              case (_, eligibilityStatus) => eligibilityStatus.isEmpty
                                            }) {
      None
    } else if (isEligible) {
      Some(FuzzyValue.Yes)
    } else {
      Some(FuzzyValue.Maybe)
    }
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
                                      isVerified: Boolean)

object PatientTrialArmGroupView {

  implicit def toPhiString(x: PatientTrialArmGroupView): PhiString = {
    import x._
    phi"PatientTrialArmGroupView(id=$id, patientId=$patientId, trialId=$trialId, hypothesisId=$hypothesisId, " +
      phi"eligibilityStatus=${Unsafe(eligibilityStatus)}, isVerified=$isVerified)"
  }
}

final case class PatientTrialArmGroupView(id: LongId[PatientTrialArmGroup],
                                          patientId: UuidId[Patient],
                                          trialId: StringId[Trial],
                                          hypothesisId: UuidId[Hypothesis],
                                          eligibilityStatus: Option[FuzzyValue],
                                          isVerified: Boolean) {

  def applyTo(trialArmGroup: PatientTrialArmGroup) = {
    trialArmGroup.copy(
      eligibilityStatus = this.eligibilityStatus,
      isVerified = this.isVerified
    )
  }
}
