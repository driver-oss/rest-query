package xyz.driver.pdsuidomain.formats.json.evidence

import java.time.LocalDate

import xyz.driver.pdsuidomain.services.PatientLabelEvidenceService
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.FuzzyValue

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

  def fromDomain(x: PatientLabelEvidenceService.Aggregated): ApiPatientLabelEvidence = {
    import x._

    ApiPatientLabelEvidence(
      id = evidence.id.id,
      value = FuzzyValue.valueToString(evidence.value),
      evidenceText = evidence.evidenceText,
      documentId = evidence.documentId.map(_.id),
      evidenceId = evidence.evidenceId.map(_.id),
      reportId = evidence.reportId.map(_.toString),
      documentType = documentType,
      date = date,
      providerType = providerType
    )
  }
}
