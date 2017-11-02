package xyz.driver.pdsuidomain.entities

import java.time.{LocalDate, LocalDateTime, ZoneId}

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.compat.Implicits._
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuicommon.validation.Validators
import xyz.driver.pdsuicommon.validation.Validators.Validator
import xyz.driver.pdsuidomain.entities.Document.Meta

import scalaz.Equal
import scalaz.syntax.equal._
import scalaz.Scalaz.stringInstance

sealed trait ProviderType {
  val id: LongId[ProviderType]
  val name: String
}

object ProviderType {

  case object MedicalOncology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](1)
    val name: String             = "Medical Oncology"
  }

  case object Surgery extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](2)
    val name: String             = "Surgery"
  }

  case object Pathology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](3)
    val name: String             = "Pathology"
  }

  case object MolecularPathology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](4)
    val name: String             = "Molecular Pathology"
  }

  case object LaboratoryMedicine extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](5)
    val name: String             = "Laboratory Medicine"
  }

  case object Radiology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](6)
    val name: String             = "Radiology"
  }

  case object InterventionalRadiology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](7)
    val name: String             = "Interventional Radiology"
  }

  case object RadiationOncology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](8)
    val name: String             = "Radiation Oncology"
  }

  case object PrimaryCare extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](9)
    val name: String             = "Primary Care"
  }

  case object Cardiology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](10)
    val name: String             = "Cardiology"
  }

  case object Dermatology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](11)
    val name: String             = "Dermatology"
  }

  case object Ophthalmology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](12)
    val name: String             = "Ophthalmology"
  }

  case object Gastroenterology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](13)
    val name: String             = "Gastroenterology"
  }

  case object Neurology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](14)
    val name: String             = "Neurology"
  }

  case object Psychiatry extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](15)
    val name: String             = "Psychiatry"
  }

  case object Gynecology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](16)
    val name: String             = "Gynecology"
  }

  case object InfectiousDisease extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](17)
    val name: String             = "Infectious Disease"
  }

  case object Immunology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](18)
    val name: String             = "Immunology"
  }

  case object Nephrology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](19)
    val name: String             = "Nephrology"
  }

  case object Rheumatology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](20)
    val name: String             = "Rheumatology"
  }

  case object Cytology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](21)
    val name: String             = "Cytology"
  }

  case object Otolaryngology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](22)
    val name: String             = "Otolaryngology"
  }

  case object Anesthesiology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](23)
    val name: String             = "Anesthesiology"
  }

  case object Urology extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](24)
    val name: String             = "Urology"
  }

  case object PalliativeCare extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](25)
    val name: String             = "Palliative Care"
  }

  case object EmergencyMedicine extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](26)
    val name: String             = "Emergency Medicine"
  }

  case object SocialWork extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](27)
    val name: String             = "Social Work"
  }

  case object NA extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](28)
    val name: String             = "N/A"
  }

  case object Other extends ProviderType {
    val id: LongId[ProviderType] = LongId[ProviderType](29)
    val name: String             = "Other"
  }

  val All = Seq[ProviderType](
    MedicalOncology,
    Surgery,
    Pathology,
    MolecularPathology,
    LaboratoryMedicine,
    Radiology,
    InterventionalRadiology,
    RadiationOncology,
    PrimaryCare,
    Cardiology,
    Dermatology,
    Ophthalmology,
    Gastroenterology,
    Neurology,
    Psychiatry,
    Gynecology,
    InfectiousDisease,
    Immunology,
    Nephrology,
    Rheumatology,
    Cytology,
    Otolaryngology,
    Anesthesiology,
    Urology,
    PalliativeCare,
    EmergencyMedicine,
    SocialWork,
    NA,
    Other
  )

  def fromString(txt: String): Option[ProviderType] = {
    All.find(_.name === txt)
  }

  implicit def toPhiString(x: ProviderType): PhiString = {
    import x._
    phi"ProviderType(id=$id, category=${Unsafe(name)})"
  }
}

sealed trait DocumentType {
  val id: LongId[DocumentType]
  val name: String
}

object DocumentType {

  case object OutpatientPhysicianNote extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](1)
    val name: String             = "Outpatient Physician Note"
  }

  case object DischargeNote extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](2)
    val name: String             = "Discharge Note"
  }

  case object LaboratoryReport extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](3)
    val name: String             = "Laboratory Report"
  }

  case object MedicationList extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](4)
    val name: String             = "Medication List"
  }

  case object HospitalizationNote extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](5)
    val name: String             = "Hospitalization Note"
  }

  case object PathologyReport extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](6)
    val name: String             = "Pathology Report"
  }

  case object RadiologyReport extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](7)
    val name: String             = "Radiology Report"
  }

  case object OperativeProcedureReport extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](8)
    val name: String             = "Operative/Procedure Report"
  }

  case object MedicationAdministration extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](9)
    val name: String             = "Medication Administration"
  }

  case object SocialWorkCaseManagementNote extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](10)
    val name: String             = "Social Work/Case Management Note"
  }

  case object NonPhysicianProviderNote extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](11)
    val name: String             = "Non-physician Provider Note"
  }

  case object Administrative extends DocumentType {
    val id: LongId[DocumentType] = LongId[DocumentType](12)
    val name: String             = "Administrative"
  }

  val All = Seq[DocumentType](
    OutpatientPhysicianNote,
    DischargeNote,
    LaboratoryReport,
    MedicationList,
    HospitalizationNote,
    PathologyReport,
    RadiologyReport,
    OperativeProcedureReport,
    MedicationAdministration,
    SocialWorkCaseManagementNote,
    NonPhysicianProviderNote,
    Administrative
  )

  def fromString(txt: String): Option[DocumentType] = {
    All.find(_.name === txt)
  }

  implicit def equal: Equal[DocumentType] = Equal.equal[DocumentType](_ == _)

  implicit def toPhiString(x: DocumentType): PhiString = {
    import x._
    phi"DocumentType(id=$id, name=${Unsafe(name)})"
  }
}

object Document {

  final case class Meta(startPage: Double, endPage: Double)

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

    def fromString(status: String): Option[Status] = status match {
      case "New"       => Some(Status.New)
      case "Organized" => Some(Status.Organized)
      case "Extracted" => Some(Status.Extracted)
      case "Done"      => Some(Status.Done)
      case "Flagged"   => Some(Status.Flagged)
      case "Archived"  => Some(Status.Archived)
      case _           => None
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

    def fromString(tpe: String): Option[RequiredType] = tpe match {
      case "OPN" => Some(RequiredType.OPN)
      case "PN"  => Some(RequiredType.PN)
      case _     => None
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

      institutionName <- Validators.nonEmpty("institutionName")(input.institutionName)

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
                          institutionName: Option[String],
                          meta: Option[TextJson[Meta]], // not null
                          startDate: Option[LocalDate], // not null
                          endDate: Option[LocalDate],
                          lastUpdate: LocalDateTime,
                          labelVersion: Int) {

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
