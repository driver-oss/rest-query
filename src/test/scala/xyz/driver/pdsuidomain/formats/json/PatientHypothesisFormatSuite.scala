package xyz.driver.pdsuidomain.formats.json

import eu.timepit.refined.numeric.NonNegative
import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.UuidId
import xyz.driver.pdsuidomain.entities.PatientHypothesis
import eu.timepit.refined.refineMV

class PatientHypothesisFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.patienthypothesis._

  "Json format for patientHypothesis" should "read and write correct JSON" in {
    val orig = PatientHypothesis(
      id = UuidId("815d9715-1089-4775-b120-3afb983b9a97"),
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      hypothesisId = UuidId("e76e2fc4-a29c-44fb-a81b-8856d06bb1d4"),
      rationale = None,
      matchedTrials = refineMV[NonNegative](1)
    )
    val writtenJson = patientHypothesisWriter.write(orig)

    writtenJson should be(
      """{"id":"815d9715-1089-4775-b120-3afb983b9a97","patientId":"748b5884-3528-4cb9-904b-7a8151d6e343",
         "hypothesisId":"e76e2fc4-a29c-44fb-a81b-8856d06bb1d4","rationale":null,"matchedTrials":1}""".parseJson)
  }

}
