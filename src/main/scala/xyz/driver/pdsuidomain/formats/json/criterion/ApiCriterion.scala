package xyz.driver.pdsuidomain.formats.json.criterion

import xyz.driver.pdsuicommon.json.Serialization.seqJsonFormat
import play.api.libs.functional.syntax._
import play.api.libs.json._
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities.{Arm, Criterion, Trial}
import xyz.driver.pdsuidomain.formats.json.label.ApiCriterionLabel
import xyz.driver.pdsuidomain.services.CriterionService.RichCriterion

final case class ApiCriterion(id: Long,
                              meta: Option[String],
                              arms: Seq[Long],
                              text: Option[String],
                              isCompound: Boolean,
                              labels: Seq[ApiCriterionLabel],
                              trialId: String) {

  def toDomain = RichCriterion(
    criterion = Criterion(
      id = LongId[Criterion](id),
      trialId = StringId[Trial](trialId),
      text,
      isCompound,
      meta.getOrElse("")
    ),
    armIds = arms.map(LongId[Arm]),
    labels = labels.map(_.toDomain(LongId[Criterion](id)))
  )
}

object ApiCriterion {

  implicit val format: Format[ApiCriterion] = (
    (JsPath \ "id").format[Long] and
      (JsPath \ "meta").formatNullable(Format(Reads { x =>
        JsSuccess(Json.stringify(x))
      }, Writes[String](Json.parse))) and
      (JsPath \ "arms").format(seqJsonFormat[Long]) and
      (JsPath \ "text").formatNullable[String] and
      (JsPath \ "isCompound").format[Boolean] and
      (JsPath \ "labels").format(seqJsonFormat[ApiCriterionLabel]) and
      (JsPath \ "trialId").format[String]
  )(ApiCriterion.apply, unlift(ApiCriterion.unapply))

  def fromDomain(richCriterion: RichCriterion) = ApiCriterion(
    id = richCriterion.criterion.id.id,
    meta = Option(richCriterion.criterion.meta),
    arms = richCriterion.armIds.map(_.id),
    text = richCriterion.criterion.text,
    isCompound = richCriterion.criterion.isCompound,
    labels = richCriterion.labels.map(ApiCriterionLabel.fromDomain),
    trialId = richCriterion.criterion.trialId.id
  )
}
