package xyz.driver.pdsuidomain.formats.json

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.entities.patient.CancerType
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{EligibilityArm, EligibilityArmDisease, EligibilityArmWithDiseases}

class EligibilityArmWithDiseasesFormatSuite extends FlatSpec with Matchers {
  import xyz.driver.pdsuidomain.formats.json.eligibilityarm._

  "Json format for EligibilityArmWithDiseases" should "read and write correct JSON" in {
    val name = "arm name"

    val arm = EligibilityArm(
      id = LongId(0),
      trialId = StringId("NCT000001"),
      name = name,
      originalName = name
    )

    val disease = EligibilityArmDisease(
      arm.id,
      disease = CancerType.Lung
    )

    val eligibilityArmWithDiseases =
      EligibilityArmWithDiseases(
        arm,
        Seq(disease)
      )

    val writtenJson = eligibilityArmWithDiseasesWriter.write(eligibilityArmWithDiseases)

    writtenJson should be(
      """{"id":0,"trialId":"NCT000001","name":"arm name","originalName":"arm name","diseases":["Lung"]}""".parseJson)

    val createArmWithDiseasesJson = """{"trialId":"NCT000001","name":"arm name","diseases":["Lung"]}""".parseJson
    val parsedArmWithDiseases     = eligibilityArmWithDiseasesReader.read(createArmWithDiseasesJson)
    parsedArmWithDiseases should be(eligibilityArmWithDiseases)

    val updateArmWithDiseasesJson      = """{"name":"new arm name","diseases":["Lung","Breast"]}""".parseJson
    val expectedUpdatedArmWithDiseases = eligibilityArmWithDiseases.copy(
      eligibilityArm = eligibilityArmWithDiseases.eligibilityArm.copy(name = "new arm name"),
      eligibilityArmDiseases = Seq(disease, disease.copy(disease = CancerType.Breast))
    )

    val parsedUpdateArmWithDiseases =
      applyUpdateToEligibilityArmWithDiseases(
        updateArmWithDiseasesJson,
        eligibilityArmWithDiseases
      )

    parsedUpdateArmWithDiseases should be(expectedUpdatedArmWithDiseases)
  }
}
