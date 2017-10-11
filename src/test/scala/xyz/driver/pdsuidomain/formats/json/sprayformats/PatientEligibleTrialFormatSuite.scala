package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.{PatientCriterionArm, PatientTrialArmGroupView, Trial}
import xyz.driver.pdsuidomain.services.PatientEligibleTrialService.RichPatientEligibleTrial

class PatientEligibleTrialFormatSuite extends FlatSpec with Matchers {
  import patienteligibletrial._

  "Json format for PatientEligibleTrial" should "read and write correct JSON" in {
    val trial = Trial(
      id = StringId("NCT000001"),
      externalId = UuidId("40892a07-c638-49d2-9795-1edfefbbcc7c"),
      status = Trial.Status.Done,
      assignee = None,
      previousStatus = None,
      previousAssignee = None,
      lastActiveUserId = None,
      lastUpdate = LocalDateTime.parse("2017-08-10T18:16:19"),
      phase = "",
      hypothesisId = Some(UuidId("e76e2fc4-a29c-44fb-a81b-8856d06bb1d4")),
      studyDesignId = Some(LongId(321)),
      originalStudyDesign = None,
      isPartner = false,
      overview = None,
      overviewTemplate = "",
      isUpdated = false,
      title = "trial title",
      originalTitle = "orig trial title"
    )
    val group = PatientTrialArmGroupView(
      id = LongId(1),
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      trialId = StringId("NCT000001"),
      hypothesisId = UuidId("e76e2fc4-a29c-44fb-a81b-8856d06bb1d4"),
      eligibilityStatus = Some(FuzzyValue.Yes),
      verifiedEligibilityStatus = Some(FuzzyValue.Yes),
      isVerified = false
    )
    val arms = List(
      PatientCriterionArm(patientCriterionId = LongId(1), armId = LongId(31), armName = "arm 31"),
      PatientCriterionArm(patientCriterionId = LongId(1), armId = LongId(32), armName = "arm 32")
    )
    val orig        = RichPatientEligibleTrial(trial, group, arms)
    val writtenJson = patientEligibleTrialWriter.write(orig)

    writtenJson should be(
      """{"id":1,"patientId":"748b5884-3528-4cb9-904b-7a8151d6e343","trialId":"NCT000001","trialTitle":"trial title",
         "hypothesisId":"e76e2fc4-a29c-44fb-a81b-8856d06bb1d4","verifiedEligibilityStatus":"Yes","isVerified":false,"arms":["arm 31","arm 32"]}""".parseJson)

    val updatePatientEligibleTrialJson      = """{"isVerified":true}""".parseJson
    val expectedUpdatedPatientTrialArmGroup = group.copy(isVerified = true)
    val parsedUpdatePatientTrialArmGroup    = applyUpdateToTrialArmGroup(updatePatientEligibleTrialJson, group)
    parsedUpdatePatientTrialArmGroup should be(expectedUpdatedPatientTrialArmGroup)
  }

}
