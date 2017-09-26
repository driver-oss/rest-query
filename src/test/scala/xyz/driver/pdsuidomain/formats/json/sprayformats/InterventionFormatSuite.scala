package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{Intervention, InterventionArm, InterventionType, InterventionWithArms}

class InterventionFormatSuite extends FlatSpec with Matchers {
  import intervention._

  "Json format for Intervention" should "read and write correct JSON" in {
    val intervention = Intervention(
      id = LongId(1),
      trialId = StringId("NCT000001"),
      name = "intervention name",
      originalName = "orig name",
      typeId = Some(LongId(10)),
      originalType = Some("orig type"),
      dosage = "",
      originalDosage = "",
      isActive = true,
      deliveryMethod = Some("pill")
    )
    val arms = List(
      InterventionArm(interventionId = intervention.id, armId = LongId(20)),
      InterventionArm(interventionId = intervention.id, armId = LongId(21)),
      InterventionArm(interventionId = intervention.id, armId = LongId(22))
    )
    val orig = InterventionWithArms(
      intervention = intervention,
      arms = arms
    )
    val writtenJson = interventionFormat.write(orig)

    writtenJson should be(
      """{"id":1,"name":"intervention name","typeId":10,"dosage":"","isActive":true,"arms":[20,21,22],
        "trialId":"NCT000001","deliveryMethod":"pill","originalName":"orig name","originalDosage":"","originalType":"orig type"}""".parseJson)

    val createInterventionJson =
      """{"id":1,"name":"intervention name","typeId":10,"dosage":"","isActive":true,"arms":[20,21,22],
        "trialId":"NCT000001","deliveryMethod":"pill"}""".parseJson
    val parsedCreateIntervention = interventionFormat.read(createInterventionJson)
    val expectedCreateIntervention = parsedCreateIntervention.copy(
      intervention = intervention.copy(id = LongId(0), originalType = None, originalName = "intervention name"),
      arms = arms.map(_.copy(interventionId = LongId(0)))
    )
    parsedCreateIntervention should be(expectedCreateIntervention)

    val updateInterventionJson = """{"dosage":"descr","arms":[21,22]}""".parseJson
    val expectedUpdatedIntervention = orig.copy(
      intervention = intervention.copy(dosage = "descr"),
      arms = List(
        InterventionArm(interventionId = intervention.id, armId = LongId(21)),
        InterventionArm(interventionId = intervention.id, armId = LongId(22))
      )
    )
    val parsedUpdateIntervention = applyUpdateToInterventionWithArms(updateInterventionJson, orig)
    parsedUpdateIntervention should be(expectedUpdatedIntervention)
  }

  "Json format for InterventionType" should "read and write correct JSON" in {
    val interventionType = InterventionType(
      id = LongId(10),
      name = "type name"
    )
    val writtenJson = interventionTypeFormat.write(interventionType)

    writtenJson should be("""{"id":10,"name":"type name"}""".parseJson)

    val parsedInterventionType = interventionTypeFormat.read(writtenJson)
    parsedInterventionType should be(interventionType)
  }

}
