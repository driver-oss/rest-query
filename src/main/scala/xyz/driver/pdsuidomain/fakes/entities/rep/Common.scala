package xyz.driver.pdsuidomain.fakes.entities.rep

import xyz.driver.core.generators
import xyz.driver.core.generators._
import xyz.driver.pdsuicommon.domain.FuzzyValue

private[rep] object Common {
  def genBoundedRange[T](from: T,
                         to: T)
                        (implicit ord: Ordering[T]): (T, T) = {
    if (ord.compare(from, to) > 0) {
      to -> from
    }
    else {
      from -> to
    }
  }

  def genBoundedRangeOption[T](from: T,
                               to: T)
                              (implicit ord: Ordering[T]): (Option[T], Option[T]) = {
    val ranges = nextOption(from)
      .map(left =>
        genBoundedRange(left, to)
      )
      .map { case (left, right) =>
        left -> nextOption(right)
      }

    ranges.map(_._1) -> ranges.flatMap(_._2)
  }

  def nextFuzzyValue(): FuzzyValue = {
    generators.oneOf[FuzzyValue](FuzzyValue.All)
  }

  def nextStartAndEndPages: (Option[Double], Option[Double]) = {
    genBoundedRangeOption[Double](nextDouble(), nextDouble())
  }

}
