package xyz.driver.pdsuidomain.fakes.entities

import xyz.driver.entities.labels.Label
import xyz.driver.pdsuidomain.entities.export.trial._
import xyz.driver.pdsuidomain.entities.{Arm, Criterion, EligibilityArm, Trial}

object export {
  import common._
  import xyz.driver.core.generators._

  def nextExportTrialArm(): ExportTrialArm =
    ExportTrialArm(armId = nextLongId[EligibilityArm], armName = nextString(100))

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
      lastReviewed = nextLocalDateTime,
      labelVersion = nextInt(100).toLong,
      arms = listOf(nextExportTrialArm()),
      criteria = listOf(nextExportTrialLabelCriterion())
    )
}
