package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuicommon.logging._

object PatientHypothesis {
  implicit def toPhiString(x: PatientHypothesis): PhiString = {
    import x._
    phi"PatientHypothesis(id=$id, patientId=$patientId, hypothesisId=$hypothesisId, " +
      phi"rationale=${Unsafe(rationale)}, matchedTrials=${Unsafe(matchedTrials)})"
  }
}

final case class PatientHypothesis(id: UuidId[PatientHypothesis],
                                   patientId: UuidId[Patient],
                                   hypothesisId: UuidId[Hypothesis],
                                   rationale: Option[String],
                                   matchedTrials: Long)
