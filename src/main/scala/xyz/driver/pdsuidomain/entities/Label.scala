package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._

final case class Category(id: LongId[Category], name: String)

object Category {
  implicit def toPhiString(x: Category): PhiString = {
    import x._
    phi"Category(id=$id, name=${Unsafe(name)})"
  }
}

final case class Label(id: LongId[Label],
                       categoryId: LongId[Category],
                       name: String,
                       description: String)

object Label {
  implicit def toPhiString(x: Label): PhiString = {
    import x._
    phi"Label($id, categoryId=${Unsafe(categoryId)}, name=${Unsafe(name)}, description=${Unsafe(description)})"
  }
}

final case class CategoryWithLabels(category: Category, labels: List[Label])

object CategoryWithLabels {
  implicit def toPhiString(x: CategoryWithLabels): PhiString = {
    import x._
    phi"CategoryWithLabels(category=$category, labels=$labels)"
  }
}
