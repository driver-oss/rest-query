package xyz.driver.server.parsers

import xyz.driver.server.parsers.errors.ParseQueryArgException
import xyz.driver.pdsuicommon.utils.Implicits.{toCharOps, toStringOps}
import fastparse.all._
import fastparse.core.Parsed
import fastparse.parsers.Intrinsics.CharPred
import play.api.routing.sird._
import xyz.driver.pdsuicommon.db.{SearchFilterBinaryOperation, SearchFilterExpr, SearchFilterNAryOperation}

import scala.util.Try

@SuppressWarnings(Array("org.wartremover.warts.Product", "org.wartremover.warts.Serializable"))
object SearchFilterParser {

  private object BinaryAtomFromTuple {
    def unapply(input: (SearchFilterExpr.Dimension, (String, String))): Option[SearchFilterExpr.Atom.Binary] = {
      val (dimensionName, (strOperation, value)) = input
      parseOperation(strOperation.toLowerCase).map { op =>
        SearchFilterExpr.Atom.Binary(dimensionName, op, value.safeTrim)
      }
    }
  }

  private object NAryAtomFromTuple {
    // Compiler warning: unchecked since it is eliminated by erasure, if we user Seq[String]
    def unapply(input: (SearchFilterExpr.Dimension, (String, Seq[_]))): Option[SearchFilterExpr.Atom.NAry] = {
      val (dimensionName, (strOperation, xs)) = input
      if (strOperation.toLowerCase == "in") {
        val values = xs.asInstanceOf[Seq[String]].map(_.safeTrim)
        Some(SearchFilterExpr.Atom.NAry(dimensionName, SearchFilterNAryOperation.In, values))
      } else {
        None
      }
    }
  }

  private val operationsMapping = {
    import xyz.driver.pdsuicommon.db.SearchFilterBinaryOperation._

    Map[String, SearchFilterBinaryOperation](
      "eq"    -> Eq,
      "noteq" -> NotEq,
      "like"  -> Like,
      "gt"    -> Gt,
      "gteq"  -> GtEq,
      "lt"    -> Lt,
      "lteq"  -> LtEq
    )
  }

  private def parseOperation(x: String): Option[SearchFilterBinaryOperation] = operationsMapping.get(x)

  private val whitespaceParser = P(CharPred(_.isSafeWhitespace))

  val dimensionParser: Parser[SearchFilterExpr.Dimension] = {
    val identParser = P(
      CharPred(c => c.isLetterOrDigit)
        .rep(min = 1)).!.map(SearchFilterExpr.Dimension(None, _))
    val pathParser = P(identParser.! ~ "." ~ identParser.!) map {
      case (left, right) => SearchFilterExpr.Dimension(Some(left), right)
    }
    P(pathParser | identParser)
  }

  private val commonOperatorParser: Parser[String] = {
    P(IgnoreCase("eq") | IgnoreCase("like") | IgnoreCase("noteq")).!
  }

  private val numericOperatorParser: Parser[String] = {
    P((IgnoreCase("gt") | IgnoreCase("lt")) ~ IgnoreCase("eq").?).!
  }

  private val naryOperatorParser: Parser[String] = P(IgnoreCase("in")).!

  private val isPositiveParser: Parser[Boolean] = P(CharIn("-+").!.?).map {
    case Some("-") => false
    case _         => true
  }

  // Exclude Unicode "digits"
  private val digitsParser: Parser[String] = P(CharIn('0' to '9').rep(min = 1).!)

  // @TODO Make complex checking here
  private val numberParser: Parser[String] = P(isPositiveParser ~ digitsParser.! ~ ("." ~ digitsParser).!.?).map {
    case (false, intPart, Some(fracPart)) => s"-$intPart.${fracPart.tail}"
    case (false, intPart, None)           => s"-$intPart"
    case (_, intPart, Some(fracPart))     => s"$intPart.${fracPart.tail}"
    case (_, intPart, None)               => s"$intPart"
  }

  private val nAryValueParser: Parser[String] = P(CharPred(_ != ',').rep(min = 1).!)

  private val binaryAtomParser: Parser[SearchFilterExpr.Atom.Binary] = P(
    dimensionParser ~ whitespaceParser ~ (
      (commonOperatorParser.! ~/ whitespaceParser ~/ AnyChar.rep(min = 1).!)
        | (numericOperatorParser.! ~/ whitespaceParser ~/ numberParser.!)
    ) ~ End
  ).map {
    case BinaryAtomFromTuple(atom) => atom
  }

  private val nAryAtomParser: Parser[SearchFilterExpr.Atom.NAry] = P(
    dimensionParser ~ whitespaceParser ~ (
      naryOperatorParser ~/ whitespaceParser ~/ nAryValueParser.!.rep(min = 1, sep = ",")
    ) ~ End
  ).map {
    case NAryAtomFromTuple(atom) => atom
  }

  private val atomParser: Parser[SearchFilterExpr.Atom] = P(binaryAtomParser | nAryAtomParser)

  def parse(queryString: QueryString): Try[SearchFilterExpr] = Try {
    queryString.getOrElse("filters", Seq.empty) match {
      case Nil => SearchFilterExpr.Empty

      case head +: Nil =>
        atomParser.parse(head) match {
          case Parsed.Success(x, _) => x
          case e: Parsed.Failure    => throw new ParseQueryArgException("filters" -> formatFailure(1, e))
        }

      case xs =>
        val parsed = xs.map(x => atomParser.parse(x))
        val failures: Seq[String] = parsed.zipWithIndex.collect {
          case (e: Parsed.Failure, index) => formatFailure(index, e)
        }

        if (failures.isEmpty) {
          val filters = parsed.collect {
            case Parsed.Success(x, _) => x
          }

          SearchFilterExpr.Intersection.create(filters: _*)
        } else {
          throw new ParseQueryArgException("filters" -> failures.mkString("; "))
        }
    }
  }

  private def formatFailure(sectionIndex: Int, e: Parsed.Failure): String = {
    s"section $sectionIndex: ${ParseError.msg(e.extra.input, e.extra.traced.expected, e.index)}"
  }

}
