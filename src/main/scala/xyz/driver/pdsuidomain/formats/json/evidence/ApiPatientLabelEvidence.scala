package xyz.driver.pdsuidomain.formats.json.evidence

import java.time.LocalDate

import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.FuzzyValue
import xyz.driver.pdsuidomain.entities.PatientLabelEvidenceView

final case class ApiPatientLabelEvidence(id: Long,
                                         value: String,
                                         evidenceText: String,
                                         documentId: Option[Long],
                                         evidenceId: Option[Long],
                                         reportId: Option[String],
                                         documentType: String,
                                         date: LocalDate,
                                         providerType: String)

object ApiPatientLabelEvidence {

  implicit val format: Format[ApiPatientLabelEvidence] = Json.format

  def fromDomain(x: PatientLabelEvidenceView) = ApiPatientLabelEvidence(
    id = x.id.id,
    value = FuzzyValue.valueToString(x.value),
    evidenceText = x.evidenceText,
    documentId = x.documentId.map(_.id),
    evidenceId = x.evidenceId.map(_.id),
    reportId = x.reportId.map(_.toString),
    documentType = x.documentType,
    date = x.date.get,
    providerType = x.providerType
  )
}