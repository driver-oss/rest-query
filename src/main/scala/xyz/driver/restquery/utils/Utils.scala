package xyz.driver.restquery.utils

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

  object Whitespace {
    private val Table: String =
      "\u2002\u3000\r\u0085\u200A\u2005\u2000\u3000" +
        "\u2029\u000B\u3000\u2008\u2003\u205F\u3000\u1680" +
        "\u0009\u0020\u2006\u2001\u202F\u00A0\u000C\u2009" +
        "\u3000\u2004\u3000\u3000\u2028\n\u2007\u3000"

    private val Multiplier: Int = 1682554634
    private val Shift: Int      = Integer.numberOfLeadingZeros(Table.length - 1)

    def matches(c: Char): Boolean = Table.charAt((Multiplier * c) >>> Shift) == c
  }

  def isSafeWhitespace(char: Char): Boolean = Whitespace.matches(char)

  // From Guava
  def isSafeControl(char: Char): Boolean =
    char <= '\u001f' || (char >= '\u007f' && char <= '\u009f')

  def safeTrim(string: String): String = {
    def shouldKeep(c: Char): Boolean = !isSafeControl(c) && !isSafeWhitespace(c)

    if (string.isEmpty) {
      ""
    } else {
      val start = string.indexWhere(shouldKeep)
      val end   = string.lastIndexWhere(shouldKeep)

      if (start >= 0 && end >= 0) {
        string.substring(start, end + 1)
      } else {
        ""
      }
    }
  }
}
