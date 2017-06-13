package xyz.driver.pdsuidomain.entities

import java.time.LocalDateTime

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.{JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonSerialize}
import xyz.driver.pdsuicommon.domain.{LongId, TextJson, User}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuicommon.validation.Validators
import xyz.driver.pdsuicommon.validation.Validators.Validator
import xyz.driver.pdsuidomain.entities.Document.Meta
import xyz.driver.pdsuicommon.compat.Implicits._

final case class ProviderType(id: LongId[ProviderType], name: String)

object ProviderType {
  implicit def toPhiString(x: ProviderType): PhiString = {
    import x._
    phi"ProviderType(id=$id, category=${Unsafe(name)})"
  }
}

final case class DocumentType(id: LongId[DocumentType], name: String)

object DocumentType {
  implicit def toPhiString(x: DocumentType): PhiString = {
    import x._
    phi"DocumentType(id=$id, name=${Unsafe(name)})"
  }
}

object Document {

  case class Meta(predicted: Option[Boolean], startPage: Double, endPage: Double) {

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

    implicit def toPhiString(x: Status): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  implicit def toPhiString(x: Document): PhiString = {
    import x._
    phi"Document(id=$id, status=$status, assignee=$assignee, previousAssignee=$previousAssignee, recordId=$recordId)"
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
        val now        = LocalDateTime.now()
        val hasInvalid = dates.exists(_.isAfter(now))

        if (hasInvalid) Validators.fail("Dates should be in the past")
        else Validators.success(true)
      }
    } yield input
  }

}

@JsonIgnoreProperties(value = Array("valid"))
case class Document(id: LongId[Document] = LongId(0L),
                    status: Document.Status,
                    previousStatus: Option[Document.Status],
                    assignee: Option[LongId[User]],
                    previousAssignee: Option[LongId[User]],
                    recordId: LongId[MedicalRecord],
                    physician: Option[String],
                    typeId: Option[LongId[DocumentType]], // not null
                    providerName: Option[String], // not null
                    providerTypeId: Option[LongId[ProviderType]], // not null
                    meta: Option[TextJson[Meta]], // not null
                    startDate: Option[LocalDateTime], // not null
                    endDate: Option[LocalDateTime],
                    lastUpdate: LocalDateTime) {

  import Document.Status._

  if (previousStatus.nonEmpty) {
    assert(AllPrevious.contains(previousStatus.get), s"Previous status has invalid value: ${previousStatus.get}")
  }

  //  TODO: with the current business logic code this constraint sometimes harmful
  //  require(status match {
  //    case Document.Status.New if assignee.isDefined => false
  //    case Document.Status.Done if assignee.isDefined => false
  //    case _ => true
  //  }, "Assignee can't be defined in New or Done statuses")

}
