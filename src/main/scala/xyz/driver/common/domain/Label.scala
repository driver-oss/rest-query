package xyz.driver.common.domain

import xyz.driver.common.logging._

case class Label(id: LongId[Label],
                 categoryId: LongId[Category],
                 name: String,
                 description: String)

object Label {
  implicit def toPhiString(x: Label): PhiString = {
    import x._
    phi"Label($id, categoryId=${Unsafe(categoryId)}, name=${Unsafe(name)}, description=${Unsafe(description)})"
  }
}
