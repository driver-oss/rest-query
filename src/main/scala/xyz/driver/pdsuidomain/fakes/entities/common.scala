package xyz.driver.pdsuidomain.fakes.entities

import java.time.{LocalDate, LocalDateTime, LocalTime}

import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.{Trial, TrialHistory}

object common {
  import xyz.driver.core.generators

  def nextUuidId[T] = UuidId[T](generators.nextUuid())

  def nextLongId[T] = LongId[T](generators.nextInt(Int.MaxValue).toLong)

  def nextStringId[T] = StringId[T](generators.nextString(maxLength = 20))

  def nextTrialStatus = generators.oneOf[Trial.Status](Trial.Status.All)

  def nextPreviousTrialStatus = generators.oneOf[Trial.Status](Trial.Status.AllPrevious)

  def nextLocalDateTime = LocalDateTime.of(nextLocalDate, LocalTime.MIDNIGHT)

  def nextLocalDate = {
    val date = generators.nextDate()
    LocalDate.of(date.year, date.month + 1, date.day + 1)
  }

  def nextCondition = generators.oneOf[Trial.Condition](Trial.Condition.All)

  def nextTrialAction = generators.oneOf[TrialHistory.Action](TrialHistory.Action.All)

  def nextTrialState = generators.oneOf[TrialHistory.State](TrialHistory.State.All)

}
