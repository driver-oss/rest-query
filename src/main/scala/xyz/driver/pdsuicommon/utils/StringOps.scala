package xyz.driver.pdsuicommon.utils

import xyz.driver.pdsuicommon.utils.Implicits.toCharOps

final class StringOps(val self: String) extends AnyVal {

  def safeTrim: String = {
    def shouldKeep(c: Char): Boolean = !c.isSafeControl && !c.isSafeWhitespace

    if (self.isEmpty) {
      ""
    } else {
      val start = self.indexWhere(shouldKeep)
      val end = self.lastIndexWhere(shouldKeep)

      if (start >= 0 && end >= 0) {
        self.substring(start, end + 1)
      } else {
        ""
      }
    }
  }
}
