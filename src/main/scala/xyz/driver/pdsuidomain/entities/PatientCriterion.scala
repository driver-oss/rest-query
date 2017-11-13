package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import xyz.driver.entities.labels.LabelValue
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.logging._

object PatientCriterion {
  implicit def toPhiString(x: PatientCriterion): PhiString = {
    import x._
    phi"PatientCriterion(id=$id, patientLabelId=$patientLabelId, trialId=${Unsafe(trialId)}, nctId=$nctId, " +
      phi"criterionId=$criterionId, criterionValue=${Unsafe(criterionValue)}, " +
      phi"isImplicitMatch=$criterionIsDefining), criterionIsDefining=${Unsafe(criterionIsDefining)}, " +
      phi"eligibilityStatus=${Unsafe(eligibilityStatus)}, verifiedEligibilityStatus=${Unsafe(verifiedEligibilityStatus)}, " +
      phi"isVerified=${Unsafe(isVerified)}, lastUpdate=${Unsafe(lastUpdate)}, inclusion=${Unsafe(inclusion)}"
  }

  /**
    * @see https://driverinc.atlassian.net/wiki/display/MTCH/EV+Business+Process
    */
  def getEligibilityStatus(criterionValue: Option[Boolean], primaryValue: LabelValue): LabelValue = {
    primaryValue match {
      case LabelValue.Unknown          => LabelValue.Unknown
      case LabelValue.Maybe            => LabelValue.Maybe
      case _ if criterionValue.isEmpty => LabelValue.Maybe
      case status =>
        LabelValue.fromBoolean(
          LabelValue.fromBoolean(
            criterionValue.getOrElse(throw new IllegalArgumentException("Criterion should not be empty"))) == status
        )
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
                                  eligibilityStatus: LabelValue,
                                  verifiedEligibilityStatus: LabelValue,
                                  isVerified: Boolean,
                                  isVisible: Boolean,
                                  lastUpdate: LocalDateTime,
                                  inclusion: Option[Boolean]) {
  import scalaz.syntax.equal._
  def isIneligibleForEv: Boolean = eligibilityStatus === LabelValue.No && isVerified
}

object PatientCriterionArm {

  implicit def toPhiString(x: PatientCriterionArm): PhiString = {
    import x._
    phi"PatientCriterionArm(patientCriterionId=$patientCriterionId, armId=$armId, armName=${Unsafe(armName)})"
  }

}

final case class PatientCriterionArm(patientCriterionId: LongId[PatientCriterion], armId: LongId[Arm], armName: String)
