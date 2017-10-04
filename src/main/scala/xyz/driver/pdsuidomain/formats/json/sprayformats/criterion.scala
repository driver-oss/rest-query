package xyz.driver.pdsuidomain.formats.json.sprayformats

import spray.json._
import xyz.driver.entities.labels.{Label, LabelCategory}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuidomain.entities._
import xyz.driver.pdsuidomain.services.CriterionService.RichCriterion

object criterion {
  import DefaultJsonProtocol._
  import common._

  implicit val criterionLabelWriter = new RootJsonWriter[CriterionLabel] {
    override def write(obj: CriterionLabel) = JsObject(
      "labelId"    -> obj.labelId.toJson,
      "categoryId" -> obj.categoryId.toJson,
      "value" -> obj.value.map {
        case true  => "Yes"
        case false => "No"
      }.toJson,
      "isDefining" -> obj.isDefining.toJson
    )
  }

  def jsValueToCriterionLabel(json: JsValue, criterionId: LongId[Criterion]): CriterionLabel = json match {
    case JsObject(fields) =>
      val labelId = fields
        .get("labelId")
        .map(_.convertTo[LongId[Label]])

      val categoryId = fields
        .get("categoryId")
        .map(_.convertTo[LongId[LabelCategory]])

      val value = fields
        .get("value")
        .map(_.convertTo[String] match {
          case "Yes" => true
          case "No"  => false
          case other =>
            deserializationError(s"Unknown `value` of CriterionLabel object: expected `yes` or `no`, but got $other")
        })

      val isDefining = fields
        .get("isDefining")
        .map(_.convertTo[Boolean])
        .getOrElse(deserializationError(s"CriterionLabel json object does not contain `isDefining` field: $json"))

      CriterionLabel(
        id = LongId(0L),
        labelId = labelId,
        criterionId = criterionId,
        categoryId = categoryId,
        value = value,
        isDefining = isDefining
      )

    case _ => deserializationError(s"Expected Json Object as CriterionLabel, but got $json")
  }

  def applyUpdateToCriterion(json: JsValue, orig: RichCriterion): RichCriterion = json match {
    case JsObject(fields) =>
      val text = fields
        .get("text")
        .map(_.convertTo[String])

      val isCompound = fields
        .get("isCompound")
        .exists(_.convertTo[Boolean])

      val meta = fields
        .get("meta")
        .map(_.convertTo[Option[String]].getOrElse("{}"))
        .getOrElse(orig.criterion.meta)

      val inclusion = fields
        .get("inclusion")
        .map(_.convertTo[Option[Boolean]])
        .getOrElse(orig.criterion.inclusion)

      val arms = fields
        .get("arms")
        .map(_.convertTo[Option[List[LongId[Arm]]]].getOrElse(List.empty[LongId[Arm]]))
        .getOrElse(orig.armIds)

      val labels = fields
        .get("labels")
        .map(_.convertTo[Option[List[JsValue]]].getOrElse(List.empty[JsValue]))
        .map(_.map(l => jsValueToCriterionLabel(l, orig.criterion.id)))
        .getOrElse(orig.labels)

      orig.copy(
        criterion = orig.criterion.copy(
          meta = meta,
          text = text,
          isCompound = isCompound,
          inclusion = inclusion
        ),
        armIds = arms,
        labels = labels
      )

    case _ => deserializationError(s"Expected Json Object as partial Criterion, but got $json")
  }

  val richCriterionFormat = new RootJsonFormat[RichCriterion] {
    override def write(obj: RichCriterion): JsValue =
      JsObject(
        "id"         -> obj.criterion.id.toJson,
        "meta"       -> Option(obj.criterion.meta).toJson,
        "arms"       -> obj.armIds.toJson,
        "text"       -> obj.criterion.text.toJson,
        "isCompound" -> obj.criterion.isCompound.toJson,
        "labels"     -> obj.labels.map(_.toJson).toJson,
        "trialId"    -> obj.criterion.trialId.toJson,
        "inclusion"  -> obj.criterion.inclusion.toJson
      )

    override def read(json: JsValue): RichCriterion = json match {
      case JsObject(fields) =>
        val trialId = fields
          .get("trialId")
          .map(_.convertTo[StringId[Trial]])
          .getOrElse(deserializationError(s"Criterion json object does not contain `trialId` field: $json"))

        val text = fields
          .get("text")
          .map(_.convertTo[String])

        val isCompound = fields
          .get("isCompound")
          .exists(_.convertTo[Boolean])

        val meta = fields
          .get("meta")
          .map(_.convertTo[String])
          .getOrElse("")

        val inclusion = fields
          .get("inclusion")
          .map(_.convertTo[Option[Boolean]])
          .getOrElse(None)

        val arms = fields
          .get("arms")
          .map(_.convertTo[List[LongId[Arm]]])
          .getOrElse(List.empty[LongId[Arm]])

        val labels = fields
          .get("labels")
          .map(_.convertTo[List[JsValue]])
          .map(_.map(l => jsValueToCriterionLabel(l, LongId(0))))
          .getOrElse(List.empty[CriterionLabel])

        RichCriterion(
          criterion = Criterion(
            id = LongId(0),
            trialId = trialId,
            text = text,
            isCompound = isCompound,
            meta = meta,
            inclusion = inclusion
          ),
          armIds = arms,
          labels = labels
        )

      case _ => deserializationError(s"Expected Json Object as Criterion, but got $json")
    }
  }

}
