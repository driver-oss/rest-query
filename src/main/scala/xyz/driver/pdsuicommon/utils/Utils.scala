package xyz.driver.pdsuicommon.utils

import java.time.LocalDateTime

object Utils {

  implicit val localDateTimeOrdering: Ordering[LocalDateTime] = Ordering.fromLessThan(_ isBefore _)

  /**
    * Hack to avoid scala compiler bug with getSimpleName
    * @see https://issues.scala-lang.org/browse/SI-2034
    */
  def getClassSimpleName(klass: Class[_]): String = {
    try {
      klass.getSimpleName
    } catch {
      case _: InternalError =>
        val fullName      = klass.getName.stripSuffix("$")
        val fullClassName = fullName.substring(fullName.lastIndexOf(".") + 1)
        fullClassName.substring(fullClassName.lastIndexOf("$") + 1)
    }
  }
}
