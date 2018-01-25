package xyz.driver.restquery.rest.parsers

import xyz.driver.restquery.rest.parsers.TestUtils._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Gen, Prop}
import org.scalatest.prop.Checkers
import org.scalatest.{FreeSpecLike, MustMatchers}

class SortingParserSuite extends FreeSpecLike with MustMatchers with Checkers {

  "parse" - {
    "single dimension" - commonTests(singleSortingQueryGen)
    "multiple dimensions in one query" - commonTests(multipleSortingQueryGen)
    "multiple queries" in {
      val r = SortingParser.parse(Set("foo", "bar"), Seq("sort" -> "foo", "sort" -> "bar"))
      r must failWith[ParseQueryArgException]
    }
  }

  private def commonTests(queryGen: Set[String] => Gen[String]): Unit = {
    "valid" in check {
      val inputGen: Gen[(Set[String], String)] = for {
        validDimensions <- dimensionsGen
        sorting         <- queryGen(validDimensions)
      } yield (validDimensions, sorting)

      Prop.forAllNoShrink(inputGen) {
        case (validDimensions, query) =>
          SortingParser.parse(validDimensions, Seq("sort" -> query)).successProp
      }
    }

    "invalid" in check {
      val inputGen: Gen[(Set[String], String)] = for {
        validDimensions <- dimensionsGen
        invalidDimensions <- dimensionsGen.filter { xs =>
                              xs.intersect(validDimensions).isEmpty
                            }
        sorting <- queryGen(invalidDimensions)
      } yield (validDimensions, sorting)

      Prop.forAllNoShrink(inputGen) {
        case (validDimensions, query) =>
          SortingParser.parse(validDimensions, Seq("sort" -> query)).failureProp
      }
    }
  }

  private val dimensionsGen: Gen[Set[String]] = for {
    unPrefixedSize <- Gen.choose(0, 3)
    prefixedSize   <- Gen.choose(0, 3)
    if (unPrefixedSize + prefixedSize) > 0

    unPrefixedDimensions <- Gen.containerOfN[Set, String](unPrefixedSize, Gen.identifier)

    prefixes   <- Gen.containerOfN[Set, String](prefixedSize, Gen.identifier)
    dimensions <- Gen.containerOfN[Set, String](prefixedSize, Gen.identifier)
  } yield {
    val prefixedDimensions = prefixes.zip(dimensions).map {
      case (prefix, dimension) => s"$prefix.$dimension"
    }
    unPrefixedDimensions ++ prefixedDimensions
  }

  private def multipleSortingQueryGen(validDimensions: Set[String]): Gen[String] = {
    val validDimensionsSeq = validDimensions.toSeq
    val indexGen           = Gen.oneOf(validDimensionsSeq.indices)
    val multipleDimensionsGen = Gen.nonEmptyContainerOf[Set, Int](indexGen).filter(_.size >= 2).map { indices =>
      indices.map(validDimensionsSeq.apply)
    }

    for {
      dimensions  <- multipleDimensionsGen
      isAscending <- Gen.containerOfN[Seq, Boolean](dimensions.size, arbitrary[Boolean])
    } yield {
      isAscending
        .zip(dimensions)
        .map {
          case (true, dimension)  => dimension
          case (false, dimension) => "-" + dimension
        }
        .mkString(",")
    }
  }

  private def singleSortingQueryGen(validDimensions: Set[String]): Gen[String] =
    for {
      isAscending <- arbitrary[Boolean]
      dimensions  <- Gen.oneOf(validDimensions.toSeq)
    } yield
      isAscending match {
        case true  => dimensions
        case false => "-" + dimensions
      }

}
