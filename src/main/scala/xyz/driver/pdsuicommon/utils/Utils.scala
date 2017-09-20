package xyz.driver.pdsuicommon.utils

import java.time.LocalDateTime
import java.util.regex.{Matcher, Pattern}

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

  def toSnakeCase(str: String): String =
    str
      .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
      .replaceAll("([a-z\\d])([A-Z])", "$1_$2")
      .toLowerCase

  def toCamelCase(str: String): String = {
    val sb = new StringBuffer()
    def loop(m: Matcher): Unit = if (m.find()) {
      m.appendReplacement(sb, m.group(1).toUpperCase())
      loop(m)
    }
    val m: Matcher = Pattern.compile("_(.)").matcher(str)
    loop(m)
    m.appendTail(sb)
    sb.toString
  }

}
