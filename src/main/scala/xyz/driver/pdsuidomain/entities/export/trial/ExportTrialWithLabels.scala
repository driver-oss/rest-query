package xyz.driver.pdsuidomain.entities.export.trial

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.{RawTrialLabel, Trial}

import scala.collection.breakOut

final case class ExportTrialWithLabels(nctId: StringId[Trial],
                                       trialId: UuidId[Trial],
                                       condition: String,
                                       lastReviewed: LocalDateTime,
                                       labelVersion: Long,
                                       arms: List[ExportTrialArm],
                                       criteria: List[ExportTrialLabelCriterion])

object ExportTrialWithLabels {

  implicit def toPhiString(x: ExportTrialWithLabels): PhiString = {
    import x._
    phi"TrialWithLabels(nctId=$nctId, trialId=$trialId, condition=${Unsafe(condition)}, " +
      phi"lastReviewed=$lastReviewed, labelVersion=${Unsafe(labelVersion)}, arms=$arms, criteria=$criteria)"
  }

  def fromRaw(rawData: List[RawTrialLabel]): ExportTrialWithLabels = {
    val trials: Set[StringId[Trial]] = rawData.map(_.nctId)(breakOut)

    assert(trials.size == 1, "There are more than one trials in the rawData")
    val trial = rawData.head

    ExportTrialWithLabels(
      nctId = trial.nctId,
      trialId = trial.trialId,
      condition = trial.condition,
      lastReviewed = trial.lastReviewed,
      labelVersion = 1, // TODO It is needed to replace this mock label version.
      arms = rawData.groupBy(_.armId).map { case (armId, rawTrials) =>
        ExportTrialArm(armId, rawTrials.head.armName)
      }(breakOut),
      criteria = rawData.groupBy { x =>
        (x.criterionId, x.labelId)
      }.map {
        case (_, rawTrialLabels) =>
          val armIds = rawTrialLabels.map(_.criterionArmId).toSet
          ExportTrialLabelCriterion.fromRaw(rawTrialLabels.head, armIds)
      }(breakOut)
    )
  }

}
