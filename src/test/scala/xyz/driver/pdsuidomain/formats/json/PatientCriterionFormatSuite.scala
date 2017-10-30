package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDateTime

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.entities.labels.LabelValue
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{PatientCriterion, PatientCriterionArm}
import xyz.driver.pdsuidomain.services.PatientCriterionService.{DraftPatientCriterion, RichPatientCriterion}

class PatientCriterionFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.patientcriterion._

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
      eligibilityStatus = Some(LabelValue.Yes),
      verifiedEligibilityStatus = None,
      isVisible = true,
      isVerified = true,
      lastUpdate = LocalDateTime.parse("2017-08-10T18:00:00"),
      inclusion = Some(true)
    )
    val arms = List(
      PatientCriterionArm(patientCriterionId = LongId(1), armId = LongId(31), armName = "arm 31"),
      PatientCriterionArm(patientCriterionId = LongId(1), armId = LongId(32), armName = "arm 32")
    )
    val richPatientCriterion = RichPatientCriterion(orig, LongId(21), arms)
    val writtenJson          = richPatientCriterionFormat.write(richPatientCriterion)

    writtenJson should be(
      """{"isVerified":true,"patientLabelId":1,"lastUpdate":"2017-08-10T18:00Z","trialId":0,
         "armList":[{"patientCriterionId":1,"armId":31,"armName":"arm 31"},{"patientCriterionId":1,
         "armId":32,"armName":"arm 32"}],"eligibilityStatus":"Yes","id":1,"nctId":"NCT00001",
         "criterionId":101,"criterionValue":true,"criterionIsDefining":false,"labelId":21,
         "isVisible":true,"criterionText":"criterion text","inclusion":true}""".parseJson)

    val updatePatientCriterionJson      = """{"verifiedEligibilityStatus":"No"}""".parseJson
    val expectedUpdatedPatientCriterion = orig.copy(verifiedEligibilityStatus = Some(LabelValue.No))
    val parsedUpdatePatientCriterion    = applyUpdateToPatientCriterion(updatePatientCriterionJson, orig)
    parsedUpdatePatientCriterion should be(expectedUpdatedPatientCriterion)

    val updateBulkPatientCriterionJson =
      """[{"id":1,"eligibilityStatus":"No"},{"id":2,"isVerified":false}]""".parseJson
    val expectedDraftPatientCriterionList = List(
      DraftPatientCriterion(id = LongId(1), eligibilityStatus = Some(LabelValue.No), isVerified = None),
      DraftPatientCriterion(id = LongId(2), eligibilityStatus = None, isVerified = Some(false))
    )
    val parsedDraftPatientCriterionList = draftPatientCriterionListReader.read(updateBulkPatientCriterionJson)
    parsedDraftPatientCriterionList should be(expectedDraftPatientCriterionList)
  }

}
