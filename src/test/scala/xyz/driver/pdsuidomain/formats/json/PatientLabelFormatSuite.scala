package xyz.driver.pdsuidomain.formats.json

import java.time.LocalDate

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.entities.labels.LabelValue
import xyz.driver.pdsuicommon.domain.{LongId, UuidId}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.PatientLabelService.RichPatientLabel

class PatientLabelFormatSuite extends FlatSpec with Matchers {

  "Json format for RichPatientLabel" should "read and write correct JSON" in {
    import xyz.driver.pdsuidomain.formats.json.patientlabel._
    val orig = PatientLabel(
      id = LongId(1),
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      labelId = LongId(20),
      primaryValue = Some(LabelValue.Yes),
      verifiedPrimaryValue = None,
      isVisible = true,
      score = 1,
      isImplicitMatch = false
    )
    val writtenJson = richPatientLabelFormat.write(RichPatientLabel(orig, isVerified = true))

    writtenJson should be(
      """{"id":1,"labelId":20,"primaryValue":"Yes","isVisible":true,"isVerified":true,
        "score":1,"isImplicitMatch":false, "patientId":"748b5884-3528-4cb9-904b-7a8151d6e343"}""".parseJson)

    val updatePatientLabelJson      = """{"verifiedPrimaryValue":"No"}""".parseJson
    val expectedUpdatedPatientLabel = orig.copy(verifiedPrimaryValue = Some(LabelValue.No))
    val parsedUpdatePatientLabel    = applyUpdateToPatientLabel(updatePatientLabelJson, orig)
    parsedUpdatePatientLabel should be(expectedUpdatedPatientLabel)
  }

  "Json format for PatientLabelEvidence" should "read and write correct JSON" in {
    import xyz.driver.pdsuidomain.formats.json.patientlabel._
    val orig = PatientLabelEvidenceView(
      id = LongId(1),
      value = LabelValue.Maybe,
      evidenceText = "evidence text",
      documentId = Some(LongId(21)),
      evidenceId = Some(LongId(10)),
      reportId = None,
      documentType = DocumentType.LaboratoryReport,
      date = Some(LocalDate.parse("2017-08-10")),
      providerType = ProviderType.EmergencyMedicine,
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      labelId = LongId(20),
      isImplicitMatch = false
    )
    val writtenJson = patientLabelEvidenceWriter.write(orig)

    writtenJson should be(
      """{"id":1,"value":"Maybe","evidenceText":"evidence text","documentId":21,"evidenceId":10,"reportId":null,
        "documentType":{"id":3,"name":"Laboratory Report"},"date":"2017-08-10",
        "providerType":{"id":26,"name":"Emergency Medicine"}}""".parseJson)
  }

  "Json format for PatientLabelDefiningCriteria" should "read and write correct JSON" in {
    import xyz.driver.pdsuidomain.formats.json.patientdefiningcriteria._
    val orig = PatientLabel(
      id = LongId(1),
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      labelId = LongId(20),
      primaryValue = Some(LabelValue.Yes),
      verifiedPrimaryValue = Some(LabelValue.Yes),
      isVisible = true,
      score = 1,
      isImplicitMatch = false
    )
    val writtenJson = patientLabelDefiningCriteriaWriter.write(orig)

    writtenJson should be("""{"id":1,"value":"Yes"}""".parseJson)
  }

}
