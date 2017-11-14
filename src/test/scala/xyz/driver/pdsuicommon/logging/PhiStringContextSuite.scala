package xyz.driver.pdsuicommon.logging

import org.scalatest.FreeSpecLike

class PhiStringContextSuite extends FreeSpecLike {

  case class Foo(x: Int, y: String) {
    val z: Boolean = true
  }

  case class Bar(y: Boolean)

  implicit def fooToPhiString(foo: Foo): PhiString = new PhiString(s"Foo(z=${foo.z})")

  "should not compile if there is no PhiString implicit" in assertDoesNotCompile(
    """val bar = Bar(true)
      |phi"bar is $bar"""".stripMargin
  )

  "should compile if there is a PhiString implicit" in assertCompiles(
    """val foo = new Foo(1, "test")
      |println(phi"foo is $foo}")""".stripMargin
  )

  "should not contain private info" in {
    val foo    = new Foo(42, "test")
    val result = phi"foo is $foo".text
    assert(!result.contains("test"))
    assert(!result.contains("42"))
  }
}
