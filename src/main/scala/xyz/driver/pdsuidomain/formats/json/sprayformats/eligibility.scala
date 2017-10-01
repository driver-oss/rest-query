package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import DefaultJsonProtocol._
import xyz.driver.core.Id
import xyz.driver.core.json._
import xyz.driver.entities.labels.LabelValue
import xyz.driver.pdsuidomain.entities.eligibility._

object eligibility {
  import xyz.driver.formats.json.assay._
  import xyz.driver.formats.json.common._
  import xyz.driver.formats.json.labels._
  import xyz.driver.formats.json.process._
  import xyz.driver.pdsuidomain.formats.json.sprayformats.document._
  import xyz.driver.pdsuidomain.formats.json.sprayformats.record._
  import xyz.driver.pdsuidomain.formats.json.sprayformats.export._

  implicit val molecularDocumentFormat: RootJsonFormat[MolecularEvidenceDocument] = jsonFormat7(
    MolecularEvidenceDocument)
  implicit val clinicalDocumentFormat: RootJsonFormat[ClinicalEvidenceDocument] = jsonFormat7(ClinicalEvidenceDocument)

  implicit val evidenceDocumentFormat: RootJsonFormat[EvidenceDocument] =
    GadtJsonFormat.create[EvidenceDocument]("documentType") {
      case _: MolecularEvidenceDocument => "Molecular"
      case _: ClinicalEvidenceDocument  => "Clinical"
    } {
      case "Molecular" => molecularDocumentFormat
      case "Clinical"  => clinicalDocumentFormat
    }

  implicit object evidenceFormat extends RootJsonFormat[Evidence] {

    override def write(evidence: Evidence): JsValue = {
      JsObject(
        "evidenceId"     -> evidence.evidenceId.toJson,
        "evidenceText"   -> evidence.evidenceText.toJson,
        "labelValue"     -> evidence.labelValue.toJson,
        "document"       -> evidence.document.toJson,
        "isPrimaryValue" -> evidence.isPrimaryValue.toJson
      )
    }

    override def read(json: JsValue): Evidence = {
      json match {
        case JsObject(fields) =>
          val evidenceId = fields
            .get("evidenceId")
            .map(_.convertTo[Id[Evidence]])

          val evidenceText = fields
            .get("evidenceText")
            .map(_.convertTo[String])
            .getOrElse(deserializationError(s"Evidence json object do not contain 'evidenceText' field: $json"))

          val labelValue = fields
            .get("labelValue")
            .map(_.convertTo[LabelValue])
            .getOrElse(deserializationError(s"Evidence json object do not contain 'labelValue' field: $json"))

          val isDriverDocument = fields
            .get("document")
            .flatMap {
              case JsObject(fieldMap) =>
                fieldMap
                  .get("isDriverDocument")
                  .map(_.convertTo[Boolean])
              case _ => deserializationError(s"Expected Json Object as 'isDriverDocument', but got $json")
            }
            .getOrElse(deserializationError(s"Evidence json object do not contain 'document' field: $json"))

          val document = customDocumentParser(isDriverDocument, fields, json)

          val isPrimaryValue = fields
            .get("isPrimaryValue")
            .map(_.convertTo[Option[Boolean]])
            .getOrElse(deserializationError(s"Evidence json object do not contain 'isPrimaryValue' field: $json"))

          Evidence(evidenceId, evidenceText, labelValue, document, isPrimaryValue)
        case _ => deserializationError(s"Expected Json Object as 'Evidence', but got $json")
      }
    }

    def customDocumentParser(isDriverDocument: Boolean,
                             fields: Map[String, JsValue],
                             json: JsValue): EvidenceDocument = {
      fields.get("document").fold { deserializationError(s"Expected Json Object as 'Document', but got $json") } {
        document =>
          if (isDriverDocument) document.convertTo[MolecularEvidenceDocument]
          else document.convertTo[ClinicalEvidenceDocument]
      }
    }
  }

  implicit def labelWithEvidenceJsonFormat: RootJsonFormat[LabelEvidence] = jsonFormat2(LabelEvidence)

  implicit def labelRankingFormat: RootJsonFormat[LabelMismatchRank]     = jsonFormat4(LabelMismatchRank)
  implicit def labelRankingsFormat: RootJsonFormat[MismatchRankedLabels] = jsonFormat2(MismatchRankedLabels)

  implicit def matchedPatientFormat: RootJsonFormat[MatchedPatient] = jsonFormat6(MatchedPatient)
}
