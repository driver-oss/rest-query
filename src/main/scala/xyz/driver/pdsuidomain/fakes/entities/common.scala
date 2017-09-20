package xyz.driver.pdsuidomain.fakes.entities

import java.time.{LocalDate, LocalDateTime, LocalTime}

import xyz.driver.pdsuicommon.domain.{LongId, StringId, UuidId}
import xyz.driver.pdsuidomain.entities.{Trial, TrialHistory}
import scala.util.Random

object common {
  import xyz.driver.core.generators

  def nextUuidId[T] = UuidId[T](generators.nextUuid())

  def nextLongId[T] = LongId[T](generators.nextInt(Int.MaxValue).toLong)

  def nextStringId[T] = StringId[T](generators.nextString(maxLength = 20))

  def nextTrialStatus = generators.oneOf[Trial.Status](Trial.Status.All)

  def nextPreviousTrialStatus = generators.oneOf[Trial.Status](Trial.Status.AllPrevious)

  def nextLocalDateTime = LocalDateTime.of(nextLocalDate, LocalTime.MIDNIGHT)

  def nextLocalDate = LocalDate.of(
    1970 + Random.nextInt(68),
    1 + Random.nextInt(12),
    1 + Random.nextInt(28) // all months have at least 28 days
  )

  def nextCondition = generators.oneOf[Trial.Condition](Trial.Condition.All)

  def nextTrialAction = generators.oneOf[TrialHistory.Action](TrialHistory.Action.All)

  def nextTrialState = generators.oneOf[TrialHistory.State](TrialHistory.State.All)

}
