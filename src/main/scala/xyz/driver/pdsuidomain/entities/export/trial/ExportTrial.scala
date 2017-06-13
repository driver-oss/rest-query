package xyz.driver.pdsuidomain.entities.export.trial

import java.time.LocalDateTime

import xyz.driver.pdsuicommon.domain.{StringId, UuidId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.Trial

case class ExportTrial(nctId: StringId[Trial],
                       trialId: UuidId[Trial],
                       condition: Trial.Condition,
                       lastReviewed: LocalDateTime)

object ExportTrial {

  implicit def toPhiString(x: ExportTrial): PhiString = {
    import x._
    phi"ExportTrial(nctId=$nctId, trialId=$trialId, condition=${Unsafe(condition)}, lastReviewed=$lastReviewed)"
  }

  def fromDomain(x: Trial) = ExportTrial(
    nctId = x.id,
    trialId = x.externalId,
    condition = x.condition,
    lastReviewed = x.lastUpdate
  )
}
