package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._

final case class Keyword(id: LongId[Keyword], keyword: String)

object Keyword {
  implicit def toPhiString(x: Keyword): PhiString = {
    import x._
    phi"Keyword(id=$id, keyword=${Unsafe(keyword)})"
  }
}

final case class KeywordWithLabels(keyword: Keyword, labels: List[Label])

object KeywordWithLabels {
  implicit def toPhiString(x: KeywordWithLabels): PhiString = {
    import x._
    phi"KeywordWithLabels(keyword=$keyword, $labels)"
  }
}

final case class KeywordLabel(keywordId: LongId[Keyword], labelId: LongId[Label])

object KeywordLabel {
  implicit def toPhiString(x: KeywordLabel): PhiString = {
    import x._
    phi"KeywordLabel($keywordId, $labelId)"
  }
}
