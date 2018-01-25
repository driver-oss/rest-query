package xyz.driver.restquery.utils

import org.scalatest.FreeSpecLike

class StringOpsSuite extends FreeSpecLike {

  "safeTrim" - {
    "empty string" in {
      assert(Utils.safeTrim("") == "")
    }

    "string with whitespace symbols" in {
      assert(Utils.safeTrim("\u2002\u3000\r\u0085\u200A\u2005\u2000\u3000") == "")
    }

    "string with control symbols" in {
      assert(Utils.safeTrim("\u001f\u007f\t\n") == "")
    }

    "whitespaces and control symbols from the left side" in {
      assert(Utils.safeTrim("\u001f\u2002\u007f\nfoo") == "foo")
    }

    "whitespaces and control symbols from the right side" in {
      assert(Utils.safeTrim("foo\u001f\u2002\u007f\n") == "foo")
    }

    "already trimmed string" in {
      assert(Utils.safeTrim("foo") == "foo")
    }
  }
}
