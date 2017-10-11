package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, StringId}
import xyz.driver.pdsuidomain.entities.{PatientCriterion, PatientCriterionArm}
import xyz.driver.pdsuidomain.services.PatientCriterionService.DraftPatientCriterion

class PatientCriterionFormatSuite extends FlatSpec with Matchers {
  import patientcriterion._

  "Json format for PatientCriterion" should "read and write correct JSON" in {
    val orig = PatientCriterion(
      id = LongId(1),
      patientLabelId = LongId(1),
      trialId = 0L,
      nctId = StringId("NCT00001"),
      criterionId = LongId(101),
      criterionText = "criterion text",
      criterionValue = Some(true),
      criterionIsDefining = false,
      eligibilityStatus = Some(FuzzyValue.Yes),
      verifiedEligibilityStatus = None,
      isVisible = true,
      isVerified = true,
      lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00")
    )
    val arms = List(
      PatientCriterionArm(patientCriterionId = LongId(1), armId = LongId(31), armName = "arm 31"),
      PatientCriterionArm(patientCriterionId = LongId(1), armId = LongId(32), armName = "arm 32")
    )
    val writtenJson = patientCriterionWriter.write((orig, LongId(21), arms))

    writtenJson should be(
      """{"id":1,"labelId":21,"nctId":"NCT00001","criterionId":101,"criterionText":"criterion text","criterionValue":"Yes",
         "criterionIsDefining":false,"criterionIsCompound":false,"eligibilityStatus":"Yes","verifiedEligibilityStatus":null,
         "isVisible":true,"isVerified":true,"lastUpdate":"2017-08-10T18:00Z","arms":["arm 31","arm 32"]}""".parseJson)

    val updatePatientCriterionJson      = """{"verifiedEligibilityStatus":"No"}""".parseJson
    val expectedUpdatedPatientCriterion = orig.copy(verifiedEligibilityStatus = Some(FuzzyValue.No))
    val parsedUpdatePatientCriterion    = applyUpdateToPatientCriterion(updatePatientCriterionJson, orig)
    parsedUpdatePatientCriterion should be(expectedUpdatedPatientCriterion)

    val updateBulkPatientCriterionJson =
      """[{"id":1,"eligibilityStatus":"No"},{"id":2,"isVerified":false}]""".parseJson
    val expectedDraftPatientCriterionList = List(
      DraftPatientCriterion(id = LongId(1), eligibilityStatus = Some(FuzzyValue.No), isVerified = None),
      DraftPatientCriterion(id = LongId(2), eligibilityStatus = None, isVerified = Some(false))
    )
    val parsedDraftPatientCriterionList = draftPatientCriterionListReader.read(updateBulkPatientCriterionJson)
    parsedDraftPatientCriterionList should be(expectedDraftPatientCriterionList)
  }

}
