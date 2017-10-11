package xyz.driver.pdsuicommon.utils

import xyz.driver.pdsuicommon.utils.Implicits.toStringOps
import org.scalatest.FreeSpecLike

class StringOpsSuite extends FreeSpecLike {

  "safeTrim" - {
    "empty string" in {
      assert("".safeTrim == "")
    }

    "string with whitespace symbols" in {
      assert("\u2002\u3000\r\u0085\u200A\u2005\u2000\u3000".safeTrim == "")
    }

    "string with control symbols" in {
      assert("\u001f\u007f\t\n".safeTrim == "")
    }

    "whitespaces and control symbols from the left side" in {
      assert("\u001f\u2002\u007f\nfoo".safeTrim == "foo")
    }

    "whitespaces and control symbols from the right side" in {
      assert("foo\u001f\u2002\u007f\n".safeTrim == "foo")
    }

    "already trimmed string" in {
      assert("foo".safeTrim == "foo")
    }
  }
}
