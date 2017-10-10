package xyz.driver.pdsuidomain.entities

import xyz.driver.entities.labels.{Label, LabelCategory, LabelValue}
import xyz.driver.pdsuicommon.domain.{LongId, TextJson}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.ExtractedData.Meta

final case class ExtractedData(id: LongId[ExtractedData] = LongId(0L),
                               documentId: LongId[Document],
                               keywordId: Option[LongId[Keyword]],
                               evidenceText: Option[String],
                               meta: Option[TextJson[Meta]]) {

  def isValid: Boolean = evidenceText.getOrElse("") != "" && meta.nonEmpty

}

object ExtractedData {

  final case class Meta(keyword: Meta.Keyword, evidence: Meta.Evidence)

  object Meta {
    final case class Evidence(pageRatio: Double, start: TextLayerPosition, end: TextLayerPosition)

    final case class TextLayerPosition(page: Integer, index: Integer, offset: Integer)

    final case class Keyword(page: Integer, pageRatio: Option[Double], index: Integer, sortIndex: String)
  }

  implicit def toPhiString(x: ExtractedData): PhiString = {
    import x._
    phi"ExtractedData(id=$id, documentId=$documentId, keywordId=$keywordId)"
  }
}

object ExtractedDataLabel {

  implicit def toPhiString(x: ExtractedDataLabel): PhiString = {
    import x._
    phi"ExtractedDataLabel(id=$id, dataId=$dataId, labelId=$labelId, categoryId=$categoryId, value=${Unsafe(value)})"
  }
}

final case class ExtractedDataLabel(id: LongId[ExtractedDataLabel],
                                    dataId: LongId[ExtractedData],
                                    labelId: Option[LongId[Label]],
                                    categoryId: Option[LongId[LabelCategory]],
                                    value: Option[LabelValue])
