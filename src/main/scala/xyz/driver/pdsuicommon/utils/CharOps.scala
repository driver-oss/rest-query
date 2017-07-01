package xyz.driver.pdsuicommon.utils

final class CharOps(val self: Char) extends AnyVal {

  import CharOps._

  def isSafeWhitespace: Boolean = Whitespace.matches(self)

  def isSafeControl: Boolean = JavaIsoControl.matches(self)
}

// From Guava
private object CharOps {

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

  object JavaIsoControl {
    def matches(c: Char): Boolean = c <= '\u001f' || (c >= '\u007f' && c <= '\u009f')
  }
}
