package xyz.driver.pdsuidomain.formats.json.export

import play.api.libs.json.{Format, Json}
import xyz.driver.pdsuidomain.entities.export.trial.ExportTrial

final case class ApiExportTrialList(trials: Seq[ApiExportTrial])

object ApiExportTrialList {

  implicit val format: Format[ApiExportTrialList] = Json.format

  def fromDomain(trialList: Seq[ExportTrial]) = ApiExportTrialList(
    trials = trialList.map(ApiExportTrial.fromDomain)
  )
}
