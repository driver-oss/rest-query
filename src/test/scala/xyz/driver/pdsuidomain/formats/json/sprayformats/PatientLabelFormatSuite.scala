package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.LocalDate

import spray.json._
import org.scalatest.{FlatSpec, Matchers}
import xyz.driver.pdsuicommon.domain.{FuzzyValue, LongId, UuidId}
import xyz.driver.pdsuidomain.entities.{PatientLabel, PatientLabelEvidenceView}
import xyz.driver.pdsuidomain.services.PatientLabelService.RichPatientLabel

class PatientLabelFormatSuite extends FlatSpec with Matchers {

  "Json format for RichPatientLabel" should "read and write correct JSON" in {
    import patientlabel._
    val orig = PatientLabel(
      id = LongId(1),
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      labelId = LongId(20),
      primaryValue = Some(FuzzyValue.Yes),
      verifiedPrimaryValue = None,
      isVisible = true,
      score = 1,
      isImplicitMatch = false
    )
    val writtenJson = richPatientLabelWriter.write(RichPatientLabel(orig, isVerified = true))

    writtenJson should be (
      """{"id":1,"labelId":20,"primaryValue":"Yes","verifiedPrimaryValue":null,"isVisible":true,"isVerified":true,
        "score":1,"isImplicitMatch":false}""".parseJson)

    val updatePatientLabelJson = """{"verifiedPrimaryValue":"No"}""".parseJson
    val expectedUpdatedPatientLabel = orig.copy(verifiedPrimaryValue = Some(FuzzyValue.No))
    val parsedUpdatePatientLabel = applyUpdateToPatientLabel(updatePatientLabelJson, orig)
    parsedUpdatePatientLabel should be(expectedUpdatedPatientLabel)
  }

  "Json format for PatientLabelEvidence" should "read and write correct JSON" in {
    import patientlabel._
    val orig = PatientLabelEvidenceView(
      id = LongId(1),
      value = FuzzyValue.Maybe,
      evidenceText = "evidence text",
      documentId = Some(LongId(21)),
      evidenceId = Some(LongId(10)),
      reportId = None,
      documentType = "document type",
      date = Some(LocalDate.parse("2017-08-10")),
      providerType = "provider type",
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      labelId = LongId(20),
      isImplicitMatch = false
    )
    val writtenJson = patientLabelEvidenceWriter.write(orig)

    writtenJson should be (
      """{"id":1,"value":"Maybe","evidenceText":"evidence text","documentId":21,"evidenceId":10,"reportId":null,
        "documentType":"document type","date":"2017-08-10","providerType":"provider type"}""".parseJson)
  }

  "Json format for PatientLabelDefiningCriteria" should "read and write correct JSON" in {
    import patientdefiningcriteria._
    val orig = PatientLabel(
      id = LongId(1),
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      labelId = LongId(20),
      primaryValue = Some(FuzzyValue.Yes),
      verifiedPrimaryValue = Some(FuzzyValue.Yes),
      isVisible = true,
      score = 1,
      isImplicitMatch = false
    )
    val writtenJson = patientLabelDefiningCriteriaWriter.write(orig)

    writtenJson should be ("""{"id":1,"value":"Yes"}""".parseJson)
  }

}
