package xyz.driver.pdsuidomain.formats.json.criterion

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.json.Serialization.seqJsonFormat
import xyz.driver.pdsuidomain.entities._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuidomain.formats.json.label.ApiCriterionLabel
import xyz.driver.pdsuidomain.services.CriterionService.RichCriterion

final case class ApiNewCriterion(meta: Option[String],
                                 arms: Option[Seq[Long]],
                                 text: Option[String],
                                 isCompound: Option[Boolean],
                                 labels: Seq[ApiCriterionLabel],
                                 trialId: String,
                                 inclusion: Option[Boolean]) {

  def toDomain = RichCriterion(
    criterion = Criterion(
      id = LongId(0L),
      meta = meta.getOrElse(""),
      trialId = StringId(trialId),
      isCompound = isCompound.getOrElse(false),
      text = text,
      inclusion = inclusion
    ),
    armIds = arms.getOrElse(Seq.empty).map(LongId[Arm]),
    labels = labels.map(_.toDomain(LongId(Long.MaxValue))) // A developer should specify right criterionId himself
  )
}

object ApiNewCriterion {

  implicit val format: Format[ApiNewCriterion] = (
    (JsPath \ "meta").formatNullable(Format(Reads { x =>
      JsSuccess(Json.stringify(x))
    }, Writes[String](Json.parse))) and
      (JsPath \ "arms").formatNullable(seqJsonFormat[Long]) and
      (JsPath \ "text").formatNullable[String] and
      (JsPath \ "isCompound").formatNullable[Boolean] and
      (JsPath \ "labels").format(seqJsonFormat[ApiCriterionLabel]) and
      (JsPath \ "trialId").format[String] and
      (JsPath \ "inclusion").formatNullable[Boolean]
  )(ApiNewCriterion.apply, unlift(ApiNewCriterion.unapply))
}
