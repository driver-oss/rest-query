package xyz.driver.restquery.rest.parsers

import java.util.UUID

import fastparse.core.Parsed
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Gen, Prop}
import org.scalatest.FreeSpecLike
import org.scalatest.prop.Checkers
import xyz.driver.restquery.query.SearchFilterBinaryOperation.Eq
import xyz.driver.restquery.query.SearchFilterExpr.Dimension
import xyz.driver.restquery.query.SearchFilterNAryOperation.{In, NotIn}
import xyz.driver.restquery.query.{SearchFilterExpr, SearchFilterNAryOperation}
import xyz.driver.restquery.rest.parsers.TestUtils._
import xyz.driver.restquery.utils.Utils
import xyz.driver.restquery.utils.Utils._

import scala.util._

object SearchFilterParserSuite {

  class UnexpectedSearchFilterExprException(x: SearchFilterExpr) extends Exception(s"unexpected $x")

}

class SearchFilterParserSuite extends FreeSpecLike with Checkers {

  import SearchFilterParserSuite._

  "parse" - {
    "should convert column names to snake case" in {
      import xyz.driver.restquery.query.SearchFilterBinaryOperation._

      val filter = SearchFilterParser.parse(
        Seq(
          "filters" -> "status IN Summarized,ReviewCriteria,Flagged,Done",
          "filters" -> "previousStatus NOTEQ New",
          "filters" -> "previousStatus NOTEQ ReviewSummary"
        ))

      assert(
        filter === Success(SearchFilterExpr.Intersection(List(
          SearchFilterExpr.Atom
            .NAry(Dimension(None, "status"), In, Seq("Summarized", "ReviewCriteria", "Flagged", "Done")),
          SearchFilterExpr.Atom
            .Binary(Dimension(None, "previous_status"), NotEq, "New"),
          SearchFilterExpr.Atom
            .Binary(Dimension(None, "previous_status"), NotEq, "ReviewSummary")
        ))))
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
            val l   = toSnakeCase(left)
            val r   = toSnakeCase(right)
            SearchFilterParser.dimensionParser.parse(raw) match {
              case Parsed.Success(Dimension(Some(`l`), `r`), _) => true
              case _                                            => false
            }
        }
      }
      "just with field name" in check {
        Prop.forAllNoShrink(Gen.identifier) { s =>
          val databaseS = Utils.toSnakeCase(s)
          SearchFilterParser.dimensionParser.parse(s) match {
            case Parsed.Success(Dimension(None, `databaseS`), _) => true
            case _                                               => false
          }
        }
      }
    }
    "atoms" - {
      "binary" - {
        "common operators" - {
          "should be parsed with text values" in check {
            import xyz.driver.restquery.query.SearchFilterBinaryOperation._

            val testQueryGen = queryGen(
              dimensionGen = Gen.identifier,
              opGen = commonBinaryOpsGen,
              valueGen = nonEmptyString
            )

            Prop.forAllNoShrink(testQueryGen) { query =>
              SearchFilterParser
                .parse(Seq("filters" -> query))
                .map {
                  case SearchFilterExpr.Atom.Binary(_, Eq | NotEq | Like, _) =>
                    true
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
              valueGen = nonEmptyString.filter { s =>
                !s.matches("^\\d+$")
              }
            )

            Prop.forAllNoShrink(testQueryGen) { query =>
              SearchFilterParser.parse(Seq("filters" -> query)).failureProp
            }
          }
        }

        "actual recordId" - {
          "should not be parsed with numeric values" in {
            val filter =
              SearchFilterParser.parse(Seq("filters" -> "recordId EQ 1"))
            assert(
              filter === Success(SearchFilterExpr.Atom
                .Binary(Dimension(None, "record_id"), Eq, Long.box(1))))
          }
        }

        "actual isVisible boolean" - {
          "should not be parsed with boolean values" in {
            val filter =
              SearchFilterParser.parse(Seq("filters" -> "isVisible EQ true"))
            assert(
              filter === Success(SearchFilterExpr.Atom
                .Binary(Dimension(None, "is_visible"), Eq, Boolean.box(true))))
          }
        }

        "actual patientId uuid" - {
          "should parse the full UUID as java.util.UUID type" in {
            val filter = SearchFilterParser.parse(Seq("filters" -> "patientId EQ 4b4879f7-42b3-4b7c-a685-5c97d9e69e7c"))
            assert(
              filter === Success(SearchFilterExpr.Atom
                .Binary(Dimension(None, "patient_id"), Eq, UUID.fromString("4b4879f7-42b3-4b7c-a685-5c97d9e69e7c"))))
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
              SearchFilterParser
                .parse(Seq("filters" -> query))
                .map {
                  case _: SearchFilterExpr.Atom.Binary => true
                  case x                               => throw new UnexpectedSearchFilterExprException(x)
                }
                .successProp
            }
          }
        }
      }

      "n-ary" - {
        "actual record Ids" - {
          "should not be parsed with text values on 'IN'" in {
            val filter = SearchFilterParser.parse(Seq("filters" -> "id IN 1,5"))
            filter match {
              case Success(_) => ()
              case Failure(t) => t.printStackTrace()
            }
            assert(
              filter === Success(SearchFilterExpr.Atom
                .NAry(Dimension(None, "id"), In, Seq(Long.box(1), Long.box(5)))))
          }
          "should not be parsed with text values on 'NOTIN'" in {
            val filter =
              SearchFilterParser.parse(Seq("filters" -> "id NOTIN 1,5"))
            filter match {
              case Success(_) => ()
              case Failure(t) => t.printStackTrace()
            }
            assert(
              filter === Success(
                SearchFilterExpr.Atom.NAry(Dimension(None, "id"), NotIn, Seq(Long.box(1), Long.box(5)))))
          }
        }

        "in" in check {
          val testQueryGen = queryGen(
            dimensionGen = Gen.identifier,
            opGen = Gen.const("in"),
            valueGen = inValuesGen
          )

          Prop.forAllNoShrink(testQueryGen) { query =>
            SearchFilterParser
              .parse(Seq("filters" -> query))
              .map {
                case SearchFilterExpr.Atom.NAry(_, SearchFilterNAryOperation.In, _) =>
                  true
                case x => throw new UnexpectedSearchFilterExprException(x)
              }
              .successProp
          }
        }

        "not in" in check {
          val testQueryGen = queryGen(
            dimensionGen = Gen.identifier,
            opGen = Gen.const("notin"),
            valueGen = inValuesGen
          )

          Prop.forAllNoShrink(testQueryGen) { query =>
            SearchFilterParser
              .parse(Seq("filters" -> query))
              .map {
                case SearchFilterExpr.Atom.NAry(_, SearchFilterNAryOperation.NotIn, _) => true
                case x                                                                 => throw new UnexpectedSearchFilterExprException(x)
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
          SearchFilterParser
            .parse(queries.map(query => "filters" -> query))
            .successProp
        }
      }
    }
  }

  private val CommonBinaryOps  = Seq("eq", "noteq", "like")
  private val NumericBinaryOps = Seq("gt", "gteq", "lt", "lteq")

  private val allBinaryOpsGen: Gen[String] =
    Gen.oneOf(CommonBinaryOps ++ NumericBinaryOps).flatMap(randomCapitalization)
  private val commonBinaryOpsGen: Gen[String] =
    Gen.oneOf(CommonBinaryOps).flatMap(randomCapitalization)
  private val numericBinaryOpsGen: Gen[String] =
    Gen.oneOf(NumericBinaryOps).flatMap(randomCapitalization)

  private val inValueCharsGen: Gen[Char] = arbitrary[Char].filter(_ != ',')

  private val nonEmptyString = arbitrary[String].filter { s =>
    !Utils.safeTrim(s).isEmpty
  }

  private val numericBinaryAtomValuesGen: Gen[String] =
    arbitrary[Long].map(_.toString)
  private val inValueGen: Gen[String] = {
    Gen
      .nonEmptyContainerOf[Seq, Char](inValueCharsGen)
      .map(_.mkString)
      .filter(s => Utils.safeTrim(s).nonEmpty)
  }
  private val inValuesGen: Gen[String] = Gen.choose(1, 5).flatMap { size =>
    Gen.containerOfN[Seq, String](size, inValueGen).map(_.mkString(","))
  }

  private def queryGen(dimensionGen: Gen[String], opGen: Gen[String], valueGen: Gen[String]): Gen[String] =
    for {
      dimension <- dimensionGen
      op        <- opGen
      value     <- valueGen
    } yield s"$dimension $op $value"

  private def randomCapitalization(input: String): Gen[String] = {
    Gen.containerOfN[Seq, Boolean](input.length, arbitrary[Boolean]).map { capitalize =>
      input.view
        .zip(capitalize)
        .map {
          case (currChar, true)  => currChar.toUpper
          case (currChar, false) => currChar
        }
        .mkString
    }
  }

}
