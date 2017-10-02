package xyz.driver.pdsuidomain.entities

import xyz.driver.entities.labels.{Label, LabelCategory}
import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.Criterion.Meta.Evidence

final case class Criterion(id: LongId[Criterion],
                           trialId: StringId[Trial],
                           text: Option[String],
                           isCompound: Boolean,
                           meta: String,
                           inclusion: Option[Boolean]) {

  def isValid: Boolean = text.nonEmpty && Option(meta).isDefined
}

object Criterion {

  final case class Meta(evidence: Evidence)

  object Meta {
    final case class Evidence(pageRatio: Double, start: TextLayerPosition, end: TextLayerPosition)
    final case class TextLayerPosition(page: Integer, index: Integer, offset: Integer)
  }

  implicit def toPhiString(x: Criterion): PhiString = {
    import x._
    phi"Criterion(id=$id, trialId=$trialId, isCompound=$isCompound)"
  }
}

final case class CriterionArm(criterionId: LongId[Criterion], armId: LongId[Arm])

object CriterionArm {

  implicit def toPhiString(x: CriterionArm): PhiString = {
    import x._
    phi"CriterionArm(criterionId=$criterionId, armId=$armId)"
  }
}

final case class CriterionLabel(id: LongId[CriterionLabel],
                                labelId: Option[LongId[Label]],
                                criterionId: LongId[Criterion],
                                categoryId: Option[LongId[LabelCategory]],
                                value: Option[Boolean],
                                isDefining: Boolean)

object CriterionLabel {

  implicit def toPhiString(x: CriterionLabel): PhiString = {
    import x._
    phi"CriterionLabel(id=$id, labelId=$labelId, criterionId=$criterionId, " +
      phi"categoryId=$categoryId, value=$value, isDefining=$isDefining)"
  }
}
