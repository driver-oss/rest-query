package xyz.driver.pdsuicommon.domain

import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils

sealed trait FuzzyValue

object FuzzyValue {
  case object Yes   extends FuzzyValue
  case object No    extends FuzzyValue
  case object Maybe extends FuzzyValue

  val All: Set[FuzzyValue] = Set(Yes, No, Maybe)

  def fromBoolean(x: Boolean): FuzzyValue = if (x) Yes else No

  implicit def toPhiString(x: FuzzyValue): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
}
