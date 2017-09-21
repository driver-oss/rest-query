package xyz.driver.pdsuicommon.domain

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils

sealed trait FuzzyValue

object FuzzyValue {
  case object Yes   extends FuzzyValue
  case object No    extends FuzzyValue
  case object Maybe extends FuzzyValue

  private val yes   = "Yes"
  private val no    = "No"
  private val maybe = "Maybe"

  val All: Set[FuzzyValue] =
    Set(Yes, No, Maybe)

  def fromBoolean(x: Boolean): FuzzyValue =
    if (x) Yes else No

  implicit def toPhiString(x: FuzzyValue): PhiString =
    Unsafe(Utils.getClassSimpleName(x.getClass))

  val fromString: PartialFunction[String, FuzzyValue] = {
    case fuzzy =>
      fuzzy.toLowerCase.capitalize match {
        case `yes`   => Yes
        case `no`    => No
        case `maybe` => Maybe
      }
  }

  def valueToString(x: FuzzyValue): String = x match {
    case Yes   => `yes`
    case No    => `no`
    case Maybe => `maybe`
  }
}
