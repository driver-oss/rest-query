package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.Arm

class ArmFormatSuite extends FlatSpec with Matchers {
  import arm._

  "Json format for Arm" should "read and write correct JSON" in {
    val arm = Arm(
      id = LongId(10),
      trialId = StringId("NCT000001"),
      name = "arm name",
      originalName = "orig arm name"
    )
    val writtenJson = armFormat.write(arm)

    writtenJson should be("""{"id":10,"trialId":"NCT000001","name":"arm name","originalName":"orig arm name"}""".parseJson)

    val createArmJson = """{"trialId":"NCT000001","name":"arm name"}""".parseJson
    val parsedArm = armFormat.read(createArmJson)
    val expectedCreatedArm = arm.copy(
      id = LongId(0),
      originalName = "arm name"
    )
    parsedArm should be(expectedCreatedArm)

    val updateArmJson = """{"name":"new arm name"}""".parseJson
    val expectedUpdatedArm = arm.copy(name = "new arm name")
    val parsedUpdateArm = applyUpdateToArm(updateArmJson, arm)
    parsedUpdateArm should be(expectedUpdatedArm)
  }

}
