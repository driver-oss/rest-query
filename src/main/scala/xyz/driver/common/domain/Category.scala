package xyz.driver.common.domain

import xyz.driver.common.logging._

case class Category(id: LongId[Category], name: String)

object Category {
  implicit def toPhiString(x: Category): PhiString = {
    import x._
    phi"Category(id=$id, name=${Unsafe(name)})"
  }
}

case class CategoryWithLabels(category: Category, labels: List[Label])

object CategoryWithLabels {
  implicit def toPhiString(x: CategoryWithLabels): PhiString = {
    import x._
    phi"CategoryWithLabels(category=$category, labels=$labels)"
  }
}
