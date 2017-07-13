package xyz.driver.pdsuidomain.entities

import java.time.{LocalDate, LocalDateTime, ZoneId}

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import xyz.driver.pdsuicommon.compat.Implicits._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuicommon.validation.Validators
import xyz.driver.pdsuicommon.validation.Validators.Validator
import xyz.driver.pdsuidomain.entities.Document.Meta

final case class ProviderType(id: LongId[ProviderType], name: String)

object ProviderType {
  sealed trait ProviderTypeName
  case object MedicalOncology         extends ProviderTypeName
  case object Surgery                 extends ProviderTypeName
  case object Pathology               extends ProviderTypeName
  case object MolecularPathology      extends ProviderTypeName
  case object LaboratoryMedicine      extends ProviderTypeName
  case object Radiology               extends ProviderTypeName
  case object InterventionalRadiology extends ProviderTypeName
  case object RadiationOncology       extends ProviderTypeName
  case object PrimaryCare             extends ProviderTypeName
  case object Cardiology              extends ProviderTypeName
  case object Dermatology             extends ProviderTypeName
  case object Ophthalmology           extends ProviderTypeName
  case object Gastroenterology        extends ProviderTypeName
  case object Neurology               extends ProviderTypeName
  case object Psychiatry              extends ProviderTypeName
  case object Gynecology              extends ProviderTypeName
  case object InfectiousDisease       extends ProviderTypeName
  case object Immunology              extends ProviderTypeName
  case object Nephrology              extends ProviderTypeName
  case object Rheumatology            extends ProviderTypeName
  case object Cytology                extends ProviderTypeName
  case object Otolaryngology          extends ProviderTypeName
  case object Anesthesiology          extends ProviderTypeName
  case object Urology                 extends ProviderTypeName
  case object PalliativeCare          extends ProviderTypeName
  case object EmergencyMedicine       extends ProviderTypeName
  case object SocialWork              extends ProviderTypeName
  case object NA                      extends ProviderTypeName
  case object Other                   extends ProviderTypeName

  def fromString(txt: String): Option[ProviderTypeName] = {
    txt match {
      case "Medical Oncology"         => Some(MedicalOncology)
      case "Surgery"                  => Some(Surgery)
      case "Pathology"                => Some(Pathology)
      case "Molecular Pathology"      => Some(MolecularPathology)
      case "LaboratoryMedicine"       => Some(LaboratoryMedicine)
      case "Radiology"                => Some(Radiology)
      case "Interventional Radiology" => Some(InterventionalRadiology)
      case "Radiation Oncology"       => Some(RadiationOncology)
      case "Primary Care"             => Some(PrimaryCare)
      case "Cardiology"               => Some(Cardiology)
      case "Dermatology"              => Some(Dermatology)
      case "Ophthalmology"            => Some(Ophthalmology)
      case "Gastroenterology"         => Some(Gastroenterology)
      case "Neurology"                => Some(Neurology)
      case "Psychiatry"               => Some(Psychiatry)
      case "Gynecology"               => Some(Gynecology)
      case "Infectious Disease"       => Some(InfectiousDisease)
      case "Immunology"               => Some(Immunology)
      case "Nephrology"               => Some(Nephrology)
      case "Rheumatology"             => Some(Rheumatology)
      case "Cytology"                 => Some(Cytology)
      case "Otolaryngology"           => Some(Otolaryngology)
      case "Anesthesiology"           => Some(Anesthesiology)
      case "Urology"                  => Some(Urology)
      case "Palliative Care"          => Some(PalliativeCare)
      case "Emergency Medicine"       => Some(EmergencyMedicine)
      case "Social Work"              => Some(SocialWork)
      case "N/A"                      => Some(NA)
      case "Other"                    => Some(Other)
      case _                          => None
    }
  }

  implicit def toPhiString(x: ProviderType): PhiString = {
    import x._
    phi"ProviderType(id=$id, category=${Unsafe(name)})"
  }
}

final case class DocumentType(id: LongId[DocumentType], name: String)

object DocumentType {
  sealed trait DocumentTypeName
  case object OutpatientPhysicianNote      extends DocumentTypeName
  case object DischargeNote                extends DocumentTypeName
  case object LaboratoryReport             extends DocumentTypeName
  case object MedicationList               extends DocumentTypeName
  case object HospitalizationNote          extends DocumentTypeName
  case object PathologyReport              extends DocumentTypeName
  case object RadiologyReport              extends DocumentTypeName
  case object OperativeProcedureReport     extends DocumentTypeName
  case object MedicationAdministration     extends DocumentTypeName
  case object SocialWorkCaseManagementNote extends DocumentTypeName
  case object NonPhysicianProviderNote     extends DocumentTypeName
  case object Administrative               extends DocumentTypeName

  def fromString(txt: String): Option[DocumentTypeName] = {
    txt match {
      case "Outpatient Physician Note"        => Some(OutpatientPhysicianNote)
      case "Discharge Note"                   => Some(DischargeNote)
      case "Laboratory Report"                => Some(LaboratoryReport)
      case "Medication List"                  => Some(MedicationList)
      case "Hospitalization Note"             => Some(HospitalizationNote)
      case "Pathology Report"                 => Some(PathologyReport)
      case "Radiology Report"                 => Some(RadiologyReport)
      case "Operative/Procedure Report"       => Some(OperativeProcedureReport)
      case "Medication Administration"        => Some(MedicationAdministration)
      case "Social Work/Case Management Note" => Some(SocialWorkCaseManagementNote)
      case "Non-physician Provider Note"      => Some(NonPhysicianProviderNote)
      case "Administrative"                   => Some(Administrative)
      case _                                  => None
    }
  }

  implicit def toPhiString(x: DocumentType): PhiString = {
    import x._
    phi"DocumentType(id=$id, name=${Unsafe(name)})"
  }
}

object Document {

  final case class Meta(predicted: Option[Boolean], startPage: Double, endPage: Double) {

    /**
      * Return a regular meta: this meta is considered as not predicted
      */
    def confirmed: Meta = copy(predicted = predicted.map(_ => false))
  }

  class DocumentStatusSerializer extends JsonSerializer[Status] {
    def serialize(value: Status, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      gen.writeString(value.toString.toUpperCase)
    }
  }

  class DocumentStatusDeserializer extends JsonDeserializer[Status] {
    def deserialize(parser: JsonParser, context: DeserializationContext): Status = {
      val value = parser.getValueAsString
      Option(value).fold[Document.Status](Status.New /* Default document status */ ) { v =>
        Status.All.find(_.toString.toUpperCase == v) match {
          case None         => throw new RuntimeJsonMappingException(s"$v is not valid Document.Status")
          case Some(status) => status
        }
      }
    }
  }

  // Product with Serializable fixes issue:
  // Set(New, Organized) has type Set[Status with Product with Serializable]
  @JsonDeserialize(using = classOf[DocumentStatusDeserializer])
  @JsonSerialize(using = classOf[DocumentStatusSerializer])
  sealed trait Status extends Product with Serializable {

    def oneOf(xs: Status*): Boolean = xs.contains(this)

    def oneOf(xs: Set[Status]): Boolean = xs.contains(this)

  }

  object Status {
    case object New       extends Status
    case object Organized extends Status
    case object Extracted extends Status
    case object Done      extends Status
    case object Flagged   extends Status
    case object Archived  extends Status

    val All         = Set[Status](New, Organized, Extracted, Done, Flagged, Archived)
    val AllPrevious = Set[Status](Organized, Extracted)

    val fromString: PartialFunction[String, Status] = {
      case "New"       => Status.New
      case "Organized" => Status.Organized
      case "Extracted" => Status.Extracted
      case "Done"      => Status.Done
      case "Flagged"   => Status.Flagged
      case "Archived"  => Status.Archived
    }

    def statusToString(x: Status): String = x match {
      case Status.New       => "New"
      case Status.Organized => "Organized"
      case Status.Extracted => "Extracted"
      case Status.Done      => "Done"
      case Status.Flagged   => "Flagged"
      case Status.Archived  => "Archived"
    }

    implicit def toPhiString(x: Status): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  sealed trait RequiredType extends Product with Serializable {

    def oneOf(xs: RequiredType*): Boolean = xs.contains(this)

    def oneOf(xs: Set[RequiredType]): Boolean = xs.contains(this)

  }

  object RequiredType {
    case object OPN extends RequiredType
    case object PN  extends RequiredType

    val All = Set[RequiredType](OPN, PN)

    val fromString: PartialFunction[String, RequiredType] = {
      case "OPN" => RequiredType.OPN
      case "PN"  => RequiredType.PN
    }

    def requiredTypeToString(x: RequiredType): String = x match {
      case RequiredType.OPN => "OPN"
      case RequiredType.PN  => "PN"
    }

    implicit def toPhiString(x: RequiredType): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  implicit def toPhiString(x: Document): PhiString = {
    import x._
    phi"Document(id=$id, status=$status, assignee=$assignee, " +
      phi"previousAssignee=$previousAssignee, lastActiveUserId=$lastActiveUserId, recordId=$recordId)"
  }

  val validator: Validator[Document, Document] = { input =>
    for {
      typeId <- Validators.nonEmpty("typeId")(input.typeId)

      providerTypeId <- Validators.nonEmpty("providerTypeId")(input.providerTypeId)

      meta <- Validators.nonEmpty("meta")(input.meta)

      startDate <- Validators.nonEmpty("startDate")(input.startDate)

      isOrderRight <- input.endDate match {
                       case Some(endDate) if startDate.isAfter(endDate) =>
                         Validators.fail("The start date should be less, than the end one")

                       case _ => Validators.success(true)
                     }

      areDatesInThePast <- {
        val dates      = List(input.startDate, input.endDate).flatten
        val now        = LocalDate.now()
        val hasInvalid = dates.exists(_.isAfter(now))

        if (hasInvalid) Validators.fail("Dates should be in the past")
        else Validators.success(true)
      }
    } yield input
  }

}

@JsonIgnoreProperties(value = Array("valid"))
final case class Document(id: LongId[Document] = LongId(0L),
                          status: Document.Status,
                          previousStatus: Option[Document.Status],
                          assignee: Option[StringId[User]],
                          previousAssignee: Option[StringId[User]],
                          lastActiveUserId: Option[StringId[User]],
                          recordId: LongId[MedicalRecord],
                          physician: Option[String],
                          typeId: Option[LongId[DocumentType]], // not null
                          providerName: Option[String], // not null
                          providerTypeId: Option[LongId[ProviderType]], // not null
                          requiredType: Option[Document.RequiredType],
                          meta: Option[TextJson[Meta]], // not null
                          startDate: Option[LocalDate], // not null
                          endDate: Option[LocalDate],
                          lastUpdate: LocalDateTime) {

  import Document.Status._

  if (previousStatus.nonEmpty) {
    assert(AllPrevious.contains(previousStatus.get), s"Previous status has invalid value: ${previousStatus.get}")
  }

  def getRequiredType(documentTypeName: String, providerTypeName: String): Option[Document.RequiredType] = {
    import DocumentType.{OutpatientPhysicianNote, PathologyReport}
    import ProviderType.MedicalOncology

    (DocumentType.fromString(documentTypeName), ProviderType.fromString(providerTypeName), startDate) match {
      case (Some(OutpatientPhysicianNote), Some(MedicalOncology), Some(date))
          if !(date.isAfter(LocalDate.now(ZoneId.of("Z"))) || date.isBefore(
            LocalDate.now(ZoneId.of("Z")).minusMonths(6))) =>
        Some(Document.RequiredType.OPN)

      case (Some(PathologyReport), _, _) => Some(Document.RequiredType.PN)

      case _ => None
    }
  }

  //  TODO: with the current business logic code this constraint sometimes harmful
  //  require(status match {
  //    case Document.Status.New if assignee.isDefined => false
  //    case Document.Status.Done if assignee.isDefined => false
  //    case _ => true
  //  }, "Assignee can't be defined in New or Done statuses")

}
