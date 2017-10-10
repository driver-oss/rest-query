package xyz.driver.pdsuidomain.formats.json.sprayformats

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import xyz.driver.entities.labels.LabelValue
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.{DocumentType, ProviderType, RecordRequestId}

class ExportFormatSuite extends FlatSpec with Matchers {
  import export._

  "Json format for ExportPatientWithLabels" should "read and write correct JSON" in {
    import xyz.driver.pdsuidomain.entities.export.patient._
    val document = ExportPatientLabelEvidenceDocument(
      documentId = LongId(101),
      requestId = RecordRequestId(UUID.fromString("7b54a75d-4197-4b27-9045-b9b6cb131be9")),
      documentType = DocumentType.OutpatientPhysicianNote,
      providerType = ProviderType.Surgery,
      date = LocalDate.parse("2017-08-10")
    )

    val labels = List(
      ExportPatientLabel(
        id = LongId(1),
        evidences = List(
          ExportPatientLabelEvidence(
            id = LongId(11),
            value = LabelValue.Yes,
            evidenceText = "evidence text 11",
            document = document
          ),
          ExportPatientLabelEvidence(
            id = LongId(12),
            value = LabelValue.No,
            evidenceText = "evidence text 12",
            document = document
          )
        )
      ),
      ExportPatientLabel(
        id = LongId(2),
        evidences = List(
          ExportPatientLabelEvidence(
            id = LongId(12),
            value = LabelValue.Yes,
            evidenceText = "evidence text 12",
            document = document
          ),
          ExportPatientLabelEvidence(
            id = LongId(13),
            value = LabelValue.Yes,
            evidenceText = "evidence text 13",
            document = document
          )
        )
      )
    )
    val patientWithLabels = ExportPatientWithLabels(
      patientId = UuidId("748b5884-3528-4cb9-904b-7a8151d6e343"),
      labelVersion = 1L,
      labels = labels
    )

    val writtenJson = patientWithLabelsFormat.write(patientWithLabels)
    writtenJson should be(
      """{"patientId":"748b5884-3528-4cb9-904b-7a8151d6e343","labelVersion":1,"labels":[{"labelId":1,"evidence":[{"evidenceId":11,
         "labelValue":"Yes","evidenceText":"evidence text 11","document":{"documentId":101,"requestId":"7b54a75d-4197-4b27-9045-b9b6cb131be9",
         "documentType":{"id":1,"name":"Outpatient Physician Note"},"providerType":{"id":2,"name":"Surgery"},"date":"2017-08-10"}},
        {"evidenceId":12,"labelValue":"No","evidenceText":"evidence text 12","document":{"documentId":101,"requestId":"7b54a75d-4197-4b27-9045-b9b6cb131be9",
         "documentType":{"id":1,"name":"Outpatient Physician Note"},"providerType":{"id":2,"name":"Surgery"},"date":"2017-08-10"}}]},
        {"labelId":2,"evidence":[{"evidenceId":12,"labelValue":"Yes","evidenceText":"evidence text 12","document":
        {"documentId":101,"requestId":"7b54a75d-4197-4b27-9045-b9b6cb131be9","documentType":{"id":1,"name":"Outpatient Physician Note"},
        "providerType":{"id":2,"name":"Surgery"},"date":"2017-08-10"}},{"evidenceId":13,"labelValue":"Yes","evidenceText":"evidence text 13",
        "document":{"documentId":101,"requestId":"7b54a75d-4197-4b27-9045-b9b6cb131be9","documentType":{"id":1,"name":"Outpatient Physician Note"},
        "providerType":{"id":2,"name":"Surgery"},"date":"2017-08-10"}}]}]}""".parseJson)
  }

  "Json format for ApiExportTrialWithLabels" should "read and write correct JSON" in {
    import xyz.driver.pdsuidomain.entities.export.trial._
    val arms = List(
      ExportTrialArm(armId = LongId(1), armName = "arm 1", diseaseList = Seq("Breast")),
      ExportTrialArm(armId = LongId(2), armName = "arm 2",  diseaseList = Seq("Breast"))
    )
    val criteriaList = List(
      ExportTrialLabelCriterion(
        criterionId = LongId(10),
        value = Some(true),
        labelId = LongId(21),
        armIds = Set(LongId(1), LongId(2)),
        criteria = "criteria 10 text",
        isCompound = false,
        isDefining = false
      ),
      ExportTrialLabelCriterion(
        criterionId = LongId(11),
        value = None,
        labelId = LongId(21),
        armIds = Set(LongId(2)),
        criteria = "criteria 11 text",
        isCompound = true,
        isDefining = false
      )
    )
    val trialWithLabels = ExportTrialWithLabels(
      nctId = StringId("NCT000001"),
      trialId = UuidId("40892a07-c638-49d2-9795-1edfefbbcc7c"),
      lastReviewed = LocalDateTime.parse("2017-08-10T18:00:00"),
      labelVersion = 1L,
      arms = arms,
      criteria = criteriaList
    )

    val writtenJson = trialWithLabelsFormat.write(trialWithLabels)
    writtenJson should be(
      """{"nctId":"NCT000001","trialId":"40892a07-c638-49d2-9795-1edfefbbcc7c","lastReviewed":"2017-08-10T18:00Z",
        "labelVersion":1,"arms":[{"armId":1,"armName":"arm 1", "diseaseList":["Breast"]},{"armId":2,"armName":"arm 2", "diseaseList":["Breast"]}],"criteria":[
        {"value":"Yes","labelId":21,"criterionId":10,"criterionText":"criteria 10 text","armIds":[1,2],"isCompound":false,"isDefining":false},
        {"value":"Unknown","labelId":21,"criterionId":11,"criterionText":"criteria 11 text","armIds":[2],"isCompound":true,"isDefining":false}]}""".parseJson)
  }

}
