package xyz.driver.pdsuicommon.db

import org.scalatest.{FreeSpecLike, MustMatchers}

class SearchFilterExprSuite extends FreeSpecLike with MustMatchers {

  "replace" - {
    "all entities are changed" in {
      val ast = SearchFilterExpr.Union(
        Seq(
          SearchFilterExpr.Intersection(
            Seq(
              SearchFilterExpr.Atom.Binary("foo", SearchFilterBinaryOperation.Gt, "10"),
              SearchFilterExpr.Atom.Binary("foo", SearchFilterBinaryOperation.Lt, "20")
            )),
          SearchFilterExpr.Atom.NAry("bar", SearchFilterNAryOperation.In, Seq("x", "y", "z")),
          SearchFilterExpr.Atom.Binary("foo", SearchFilterBinaryOperation.Eq, "40")
        ))

      val newAst = ast.replace {
        case x: SearchFilterExpr.Atom.Binary if x.dimension.name == "foo" =>
          x.copy(dimension = x.dimension.copy(name = "bar"))
      }

      val result = newAst.find {
        case x: SearchFilterExpr.Atom.Binary => x.dimension.name == "foo"
        case _                               => false
      }

      result mustBe empty
    }
  }

}
