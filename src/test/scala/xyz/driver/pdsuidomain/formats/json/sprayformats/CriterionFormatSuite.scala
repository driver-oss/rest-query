package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{EligibilityArm, Criterion, CriterionLabel}
import xyz.driver.pdsuidomain.services.CriterionService.RichCriterion

class CriterionFormatSuite extends FlatSpec with Matchers {
  import criterion._

  "Json format for Criterion" should "read and write correct JSON" in {
    val criterion = Criterion(
      id = LongId(10),
      trialId = StringId("NCT000001"),
      text = Some("text"),
      isCompound = false,
      meta = "{}",
      inclusion = None
    )
    val labels = List(
      CriterionLabel(
        id = LongId(1L),
        labelId = Some(LongId(101)),
        criterionId = criterion.id,
        categoryId = Some(LongId(3)),
        value = Some(true),
        isDefining = true
      ),
      CriterionLabel(
        id = LongId(2L),
        labelId = Some(LongId(102)),
        criterionId = criterion.id,
        categoryId = Some(LongId(3)),
        value = Some(false),
        isDefining = true
      )
    )
    val arms = List(LongId[EligibilityArm](20), LongId[EligibilityArm](21), LongId[EligibilityArm](21))
    val richCriterion = RichCriterion(
      criterion = criterion,
      armIds = arms,
      labels = labels
    )
    val writtenJson = richCriterionFormat.write(richCriterion)

    writtenJson should be(
      """{"text":"text","isCompound":false,"trialId":"NCT000001","inclusion":null,"arms":[20,21,21],"id":10,"meta":"{}",
        "labels":[{"labelId":101,"categoryId":3,"value":"Yes","isDefining":true},
        {"labelId":102,"categoryId":3,"value":"No","isDefining":true}]}""".parseJson)

    val createCriterionJson =
      """{"text":"text","isCompound":false,"trialId":"NCT000001","inclusion":null,
        "arms":[20,21,21],"meta":"{}","labels":[{"labelId":101,"categoryId":3,"value":"Yes","isDefining":true},
        {"labelId":102,"categoryId":3,"value":"No","isDefining":true}]}""".parseJson
    val parsedRichCriterion = richCriterionFormat.read(createCriterionJson)
    val expectedRichCriterion = richCriterion.copy(
      criterion = criterion.copy(id = LongId(0)),
      labels = labels.map(_.copy(id = LongId(0), criterionId = LongId(0)))
    )
    parsedRichCriterion should be(expectedRichCriterion)

    val updateCriterionJson = """{"meta":null,"text":"new text","isCompound":true,"inclusion":true}""".parseJson
    val expectedUpdatedCriterion = richCriterion.copy(
      criterion = criterion.copy(
        text = Some("new text"),
        isCompound = true,
        meta = "{}",
        inclusion = Some(true)
      ))
    val parsedUpdateCriterion = applyUpdateToCriterion(updateCriterionJson, richCriterion)
    parsedUpdateCriterion should be(expectedUpdatedCriterion)
  }

}
