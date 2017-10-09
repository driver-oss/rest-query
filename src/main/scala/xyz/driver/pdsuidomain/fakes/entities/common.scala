package xyz.driver.pdsuidomain.fakes.entities

import java.time.{LocalDate, LocalDateTime, LocalTime}

import xyz.driver.core.generators.{nextDouble, nextOption}
import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.{Trial, TrialHistory}

import scala.util.Random

object common {
  import xyz.driver.core.generators

  def nextUuidId[T]: UuidId[T] = UuidId[T](generators.nextUuid())

  def nextLongId[T]: LongId[T] = LongId[T](generators.nextInt(Int.MaxValue).toLong)

  def nextStringId[T]: StringId[T] = StringId[T](generators.nextString(maxLength = 20))

  def nextTrialStatus: Trial.Status = generators.oneOf[Trial.Status](Trial.Status.All)

  def nextPreviousTrialStatus: Trial.Status = generators.oneOf[Trial.Status](Trial.Status.AllPrevious)

  def nextLocalDateTime: LocalDateTime = LocalDateTime.of(nextLocalDate, LocalTime.MIDNIGHT)

  def nextLocalDate: LocalDate = LocalDate.of(
    1970 + Random.nextInt(68),
    1 + Random.nextInt(12),
    1 + Random.nextInt(28) // all months have at least 28 days
  )

  def nextCondition = generators.oneOf[Trial.Condition](Trial.Condition.All)

  def nextTrialAction = generators.oneOf[TrialHistory.Action](TrialHistory.Action.All)

  def nextTrialState = generators.oneOf[TrialHistory.State](TrialHistory.State.All)

  def genBoundedRange[T](from: T, to: T)(implicit ord: Ordering[T]): (T, T) = {
    if (ord.compare(from, to) > 0) {
      to -> from
    } else {
      from -> to
    }
  }

  def genBoundedRangeOption[T](from: T, to: T)(implicit ord: Ordering[T]): (Option[T], Option[T]) = {
    val ranges = nextOption(from).map { left =>
      val range = genBoundedRange(left, to)
      range._1 -> nextOption(range._2)
    }

    ranges.map(_._1) -> ranges.flatMap(_._2)
  }

  def nextStartAndEndPages: (Option[Double], Option[Double]) =
    genBoundedRangeOption[Double](nextDouble(), nextDouble())

}
