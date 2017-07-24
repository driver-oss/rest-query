package xyz.driver.pdsuidomain.entities

import java.nio.channels.ReadableByteChannel
import java.time.LocalDateTime

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonProperty, JsonSubTypes, JsonTypeInfo}
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuidomain.entities.MedicalRecord.Meta
import xyz.driver.pdsuidomain.entities.MedicalRecord.Meta.{Duplicate, Reorder, Rotation}

object MedicalRecord {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
  @JsonSubTypes(
    Array(
      new Type(value = classOf[Duplicate], name = "duplicate"),
      new Type(value = classOf[Reorder], name = "reorder"),
      new Type(value = classOf[Rotation], name = "rotation")
    ))
  trait Meta {
    @JsonProperty("type") def metaType: String
    def predicted: Option[Boolean]

    /**
      * Return a regular meta: this meta is considered as not predicted
      */
    def confirmed: Meta
  }

  object Meta {

    final case class Duplicate(predicted: Option[Boolean],
                               startPage: Double,
                               endPage: Double,
                               startOriginalPage: Double,
                               endOriginalPage: Option[Double])
        extends Meta {
      override val metaType             = "duplicate"
      override def confirmed: Duplicate = copy(predicted = predicted.map(_ => false))
    }

    object Duplicate {
      implicit def toPhiString(x: Duplicate): PhiString = {
        import x._
        phi"Duplicate(predicted=${x.predicted}, startPage=${Unsafe(startPage)}, endPage=${Unsafe(endPage)}, " +
          phi"startOriginalPage=${Unsafe(startOriginalPage)}, endOriginalPage=${Unsafe(endOriginalPage)}"
      }
    }

    final case class Reorder(predicted: Option[Boolean], items: Seq[Int]) extends Meta {
      override val metaType           = "reorder"
      override def confirmed: Reorder = copy(predicted = predicted.map(_ => false))
    }

    object Reorder {
      implicit def toPhiString(x: Reorder): PhiString = {
        import x._
        phi"Reorder(predicted=${x.predicted}, items=${Unsafe(items.toString)})"
      }
    }

    final case class Rotation(predicted: Option[Boolean], items: Map[String, Int]) extends Meta {
      override val metaType            = "rotation"
      override def confirmed: Rotation = copy(predicted = predicted.map(_ => false))
    }

    object Rotation {
      implicit def toPhiString(x: Rotation): PhiString = {
        import x._
        phi"Rotation(predicted=${x.predicted}, items=${Unsafe(items.toString)})"
      }
    }

    implicit def toPhiString(input: Meta): PhiString = input match {
      case x: Duplicate => Duplicate.toPhiString(x)
      case x: Reorder   => Reorder.toPhiString(x)
      case x: Rotation  => Rotation.toPhiString(x)
    }

  }

  // Product with Serializable fixes issue:
  // Set(New, Cleaned) has type Set[Status with Product with Serializable]
  sealed trait Status extends Product with Serializable {

    def oneOf(xs: Status*): Boolean = xs.contains(this)

    def oneOf(xs: Set[Status]): Boolean = xs.contains(this)

  }
  object Status {
    case object Unprocessed   extends Status
    case object PreCleaning   extends Status
    case object New           extends Status
    case object Cleaned       extends Status
    case object PreOrganized  extends Status
    case object PreOrganizing extends Status
    case object Reviewed      extends Status
    case object Organized     extends Status
    case object Done          extends Status
    case object Flagged       extends Status
    case object Archived      extends Status

    def fromString(status: String): Option[Status] = status match {
      case "Unprocessed"   => Some(Unprocessed)
      case "PreCleaning"   => Some(PreCleaning)
      case "New"           => Some(New)
      case "Cleaned"       => Some(Cleaned)
      case "PreOrganized"  => Some(PreOrganized)
      case "PreOrganizing" => Some(PreOrganizing)
      case "Reviewed"      => Some(Reviewed)
      case "Organized"     => Some(Organized)
      case "Done"          => Some(Done)
      case "Flagged"       => Some(Flagged)
      case "Archived"      => Some(Archived)
      case _               => None
    }

    val All = Set[Status](
      Unprocessed,
      PreCleaning,
      New,
      Cleaned,
      PreOrganized,
      PreOrganizing,
      Reviewed,
      Organized,
      Done,
      Flagged,
      Archived
    )

    val AllPrevious = Set[Status](New, Cleaned, Reviewed, Organized)

    implicit def toPhiString(x: Status): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  sealed trait PdfSource

  object PdfSource {
    case object Empty extends PdfSource

    /** @param createResource Constructor of the resource which is represents the file */
    final case class Channel(createResource: () => ReadableByteChannel) extends PdfSource
  }

  implicit def toPhiString(x: MedicalRecord): PhiString = {
    import x._
    phi"MedicalRecord(id=$id, status=$status, assignee=$assignee, " +
      phi"previousAssignee=$previousAssignee, lastActiveUserId=$lastActiveUserId)"
  }
}

final case class MedicalRecord(id: LongId[MedicalRecord],
                               status: MedicalRecord.Status,
                               previousStatus: Option[MedicalRecord.Status],
                               assignee: Option[StringId[User]],
                               previousAssignee: Option[StringId[User]],
                               lastActiveUserId: Option[StringId[User]],
                               patientId: UuidId[Patient],
                               requestId: RecordRequestId,
                               disease: String,
                               caseId: Option[CaseId],
                               physician: Option[String],
                               meta: Option[TextJson[List[Meta]]],
                               predictedMeta: Option[TextJson[List[Meta]]],
                               predictedDocuments: Option[TextJson[List[Document]]],
                               lastUpdate: LocalDateTime) {

  import MedicalRecord.Status._

  if (previousStatus.nonEmpty) {
    assert(AllPrevious.contains(previousStatus.get), s"Previous status has invalid value: ${previousStatus.get}")
  }

  //  TODO: with the current business logic code this constraint sometimes harmful
  //  require(status match {
  //    case MedicalRecord.Status.Done if assignee.isDefined => false
  //    case _ => true
  //  }, "Assignee can't be defined in Done status")
}
