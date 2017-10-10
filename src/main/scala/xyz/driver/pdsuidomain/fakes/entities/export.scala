package xyz.driver.pdsuidomain.fakes.entities

import xyz.driver.entities.labels.Label
import xyz.driver.pdsuidomain.entities.{Arm, Criterion, Trial}
import xyz.driver.pdsuidomain.entities.export.trial._

object export {
  import xyz.driver.core.generators._
  import common._

  def nextExportTrialArm(): ExportTrialArm =
    ExportTrialArm(armId = nextLongId[Arm], armName = nextString(100))

  def nextExportTrialLabelCriterion(): ExportTrialLabelCriterion =
    ExportTrialLabelCriterion(
      criterionId = nextLongId[Criterion],
      value = nextOption[Boolean](nextBoolean()),
      labelId = nextLongId[Label],
      armIds = setOf(nextLongId[Arm]),
      criteria = nextString(100),
      isCompound = nextBoolean(),
      isDefining = nextBoolean()
    )

  def nextExportTrialWithLabels(): ExportTrialWithLabels =
    ExportTrialWithLabels(
      nctId = nextStringId[Trial],
      trialId = nextUuidId[Trial],
      disease = nextString(100),
      lastReviewed = nextLocalDateTime,
      labelVersion = nextInt(100).toLong,
      arms = listOf(nextExportTrialArm()),
      criteria = listOf(nextExportTrialLabelCriterion())
    )
}
