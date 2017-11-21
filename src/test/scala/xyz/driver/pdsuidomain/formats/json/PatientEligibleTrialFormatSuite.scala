package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import xyz.driver.entities.labels.LabelValue
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.{PatientCriterionArm, PatientTrialArmGroupView, Trial}
import xyz.driver.pdsuidomain.services.PatientEligibleTrialService.RichPatientEligibleTrial

class PatientEligibleTrialFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.patienteligibletrial._

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
      eligibilityStatus = LabelValue.Yes,
      verifiedEligibilityStatus = LabelValue.Yes,
      isVerified = false
    )
    val arms = List(
      PatientCriterionArm(patientCriterionId = LongId(1), armId = LongId(31), armName = "arm 31"),
      PatientCriterionArm(patientCriterionId = LongId(1), armId = LongId(32), armName = "arm 32")
    )
    val orig        = RichPatientEligibleTrial(trial, group, arms)
    val writtenJson = patientEligibleTrialFormat.write(orig)

    writtenJson should be(
      """{"trial":{"isPartner":false,"assignee":null,"lastUpdate":"2017-08-10T18:16:19Z","previousStatus":null,
        "isUpdated":false,"overviewTemplate":"","phase":"","originalStudyDesignId":null,
        "hypothesisId":"e76e2fc4-a29c-44fb-a81b-8856d06bb1d4","originalTitle":"orig trial title","studyDesignId":321,
        "lastActiveUser":null,"externalid":"40892a07-c638-49d2-9795-1edfefbbcc7c","id":"NCT000001","status":"Done",
        "overview":null,"previousAssignee":null,"title":"trial title"},"group":{"isVerified":false
        ,"patientId":"748b5884-3528-4cb9-904b-7a8151d6e343","hypothesisId":"e76e2fc4-a29c-44fb-a81b-8856d06bb1d4",
        "verifiedEligibilityStatus":"Yes","trialId":"NCT000001","eligibilityStatus":"Yes","id":1},
        "arms":[{"patientCriterionId":1,"armId":31,"armName":"arm 31"},{"patientCriterionId":1,"armId":32,"armName":"arm 32"}]}""".parseJson)

    val updatePatientEligibleTrialJson      = """{"group":{"isVerified":true}}""".parseJson
    val expectedUpdatedPatientTrialArmGroup = group.copy(isVerified = true)
    val parsedUpdatePatientTrialArmGroup    = applyUpdateToTrialArmGroup(updatePatientEligibleTrialJson, group)
    parsedUpdatePatientTrialArmGroup should be(expectedUpdatedPatientTrialArmGroup)
  }

}
