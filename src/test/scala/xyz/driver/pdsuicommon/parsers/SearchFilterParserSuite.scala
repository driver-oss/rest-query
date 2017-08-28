package xyz.driver.pdsuicommon.parsers

import xyz.driver.pdsuicommon.db.SearchFilterExpr.Dimension
import xyz.driver.pdsuicommon.db.{SearchFilterBinaryOperation, SearchFilterExpr, SearchFilterNAryOperation}
import xyz.driver.pdsuicommon.utils.Implicits.toStringOps
import xyz.driver.pdsuicommon.parsers.TestUtils._
import fastparse.core.Parsed
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Gen, Prop}
import org.scalatest.FreeSpecLike
import org.scalatest.prop.Checkers
import xyz.driver.pdsuicommon.db.SearchFilterNAryOperation.In
import xyz.driver.pdsuicommon.utils.Utils._

import scala.util.Success

object SearchFilterParserSuite {

  class UnexpectedSearchFilterExprException(x: SearchFilterExpr) extends Exception(s"unexpected $x")

}

class SearchFilterParserSuite extends FreeSpecLike with Checkers {

  import SearchFilterParserSuite._

  "parse" - {
    "should convert column names to snake case" in {
      import SearchFilterBinaryOperation._

      val filter = SearchFilterParser.parse(Seq(
        "filters" -> "status IN Summarized,ReviewCriteria,Flagged,Done",
        "filters" -> "previousStatus NOTEQ New",
        "filters" -> "previousStatus NOTEQ ReviewSummary"
      ))

      assert(filter === Success(SearchFilterExpr.Intersection(List(
        SearchFilterExpr.Atom.NAry(Dimension(None, "status"), In, Seq("Summarized", "ReviewCriteria", "Flagged", "Done")),
        SearchFilterExpr.Atom.Binary(Dimension(None, "previous_status"), NotEq, "New"),
        SearchFilterExpr.Atom.Binary(Dimension(None, "previous_status"), NotEq, "ReviewSummary")))))
    }
    "dimensions" - {
      "with table name" in check {
        val dimensionGen = {
          for (left <- Gen.identifier; right <- Gen.identifier)
            yield left -> right
        }
        Prop.forAllNoShrink(dimensionGen) {
          case (left, right) =>
            val raw = s"$left.$right"
            val l = toSnakeCase(left)
            val r = toSnakeCase(right)
              SearchFilterParser.dimensionParser.parse(raw) match {
              case Parsed.Success(Dimension(Some(`l`), `r`), _) => true
              case _ => false
            }
        }
      }
      "just with field name" in check {
        Prop.forAllNoShrink(Gen.identifier) { s =>
          SearchFilterParser.dimensionParser.parse(s) match {
            case Parsed.Success(Dimension(None, `s`), _) => true
            case _ => false
          }
        }
      }
    }
    "atoms" - {
      "binary" - {
        "common operators" - {
          "should be parsed with text values" in check {
            import SearchFilterBinaryOperation._

            val testQueryGen = queryGen(
              dimensionGen = Gen.identifier,
              opGen = commonBinaryOpsGen,
              valueGen = nonEmptyString
            )

            Prop.forAllNoShrink(testQueryGen) { query =>
              SearchFilterParser.parse(Seq("filters" -> query))
                .map {
                  case SearchFilterExpr.Atom.Binary(_, Eq | NotEq | Like, _) => true
                  case x => throw new UnexpectedSearchFilterExprException(x)
                }
                .successProp
            }
          }
        }

        "numeric operators" - {
          "should not be parsed with text values" in check {
            val testQueryGen = queryGen(
              dimensionGen = Gen.identifier,
              opGen = numericBinaryOpsGen,
              valueGen = nonEmptyString.filter { s => !s.matches("^\\d+$") }
            )

            Prop.forAllNoShrink(testQueryGen) { query =>
              SearchFilterParser.parse(Seq("filters" -> query)).failureProp
            }
          }
        }

        "all operators" - {
          "should be parsed with numeric values" in check {
            val testQueryGen = queryGen(
              dimensionGen = Gen.identifier,
              opGen = allBinaryOpsGen,
              valueGen = numericBinaryAtomValuesGen
            )

            Prop.forAllNoShrink(testQueryGen) { query =>
              SearchFilterParser.parse(Seq("filters" -> query))
                .map {
                  case _: SearchFilterExpr.Atom.Binary => true
                  case x => throw new UnexpectedSearchFilterExprException(x)
                }
                .successProp
            }
          }
        }
      }

      "n-ary" - {
        "in" in check {
          val testQueryGen = queryGen(
            dimensionGen = Gen.identifier,
            opGen = Gen.const("in"),
            valueGen = inValuesGen
          )

          Prop.forAllNoShrink(testQueryGen) { query =>
            SearchFilterParser.parse(Seq("filters" -> query))
              .map {
                case SearchFilterExpr.Atom.NAry(_, SearchFilterNAryOperation.In, _) => true
                case x => throw new UnexpectedSearchFilterExprException(x)
              }
              .successProp
          }
        }
      }
    }

    "intersections" - {
      "should be parsed" in check {
        val commonAtomsGen = queryGen(
          dimensionGen = Gen.identifier,
          opGen = commonBinaryOpsGen,
          valueGen = nonEmptyString
        )

        val numericAtomsGen = queryGen(
          dimensionGen = Gen.identifier,
          opGen = numericBinaryOpsGen,
          valueGen = numericBinaryAtomValuesGen
        )

        val allAtomsGen = Gen.oneOf(commonAtomsGen, numericAtomsGen)
        val intersectionsGen = Gen.choose(1, 3).flatMap { size =>
          Gen.containerOfN[Seq, String](size, allAtomsGen)
        }

        Prop.forAllNoShrink(intersectionsGen) { queries =>
          SearchFilterParser.parse(queries.map(query => "filters" -> query)).successProp
        }
      }
    }
  }

  private val CommonBinaryOps = Seq("eq", "noteq", "like")
  private val NumericBinaryOps = Seq("gt", "gteq", "lt", "lteq")

  private val allBinaryOpsGen: Gen[String] = Gen.oneOf(CommonBinaryOps ++ NumericBinaryOps).flatMap(randomCapitalization)
  private val commonBinaryOpsGen: Gen[String] = Gen.oneOf(CommonBinaryOps).flatMap(randomCapitalization)
  private val numericBinaryOpsGen: Gen[String] = Gen.oneOf(NumericBinaryOps).flatMap(randomCapitalization)

  private val inValueCharsGen: Gen[Char] = arbitrary[Char].filter(_ != ',')

  private val nonEmptyString = arbitrary[String].filter { s => !s.safeTrim.isEmpty }

  private val numericBinaryAtomValuesGen: Gen[String] = arbitrary[BigInt].map(_.toString)
  private val inValueGen: Gen[String] = {
    Gen.nonEmptyContainerOf[Seq, Char](inValueCharsGen).map(_.mkString).filter(_.safeTrim.nonEmpty)
  }
  private val inValuesGen: Gen[String] = Gen.choose(1, 5).flatMap { size =>
    Gen.containerOfN[Seq, String](size, inValueGen).map(_.mkString(","))
  }

  private def queryGen(dimensionGen: Gen[String], opGen: Gen[String], valueGen: Gen[String]): Gen[String] = for {
    dimension <- dimensionGen
    op <- opGen
    value <- valueGen
  } yield s"$dimension $op $value"

  private def randomCapitalization(input: String): Gen[String] = {
    Gen.containerOfN[Seq, Boolean](input.length, arbitrary[Boolean]).map { capitalize =>
      input.view.zip(capitalize).map {
        case (currChar, true) => currChar.toUpper
        case (currChar, false) => currChar
      }.mkString
    }
  }

}
