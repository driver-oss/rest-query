package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, StringId}
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
            FuzzyValue.fromBoolean(
              criterionValue.getOrElse(throw new IllegalArgumentException("Criterion should not be empty"))) == status
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
  def isIneligibleForEv: Boolean = eligibilityStatus.contains(FuzzyValue.No) && isVerified
}

object PatientCriterionArm {

  implicit def toPhiString(x: PatientCriterionArm): PhiString = {
    import x._
    phi"PatientCriterionArm(patientCriterionId=$patientCriterionId, armId=$armId, armName=${Unsafe(armName)})"
  }

}

final case class PatientCriterionArm(patientCriterionId: LongId[PatientCriterion],
                                     armId: LongId[Arm],
                                     armName: String)
