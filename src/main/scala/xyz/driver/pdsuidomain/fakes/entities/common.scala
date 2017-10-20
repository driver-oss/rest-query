package xyz.driver.pdsuidomain.fakes.entities

import java.time.{LocalDate, LocalDateTime, LocalTime}

import xyz.driver.core.generators._
import xyz.driver.entities.common.FullName
import xyz.driver.entities.patient.CancerType
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue
import xyz.driver.pdsuicommon.domain.{LongId, StringId, TextJson, UuidId}
import xyz.driver.pdsuidomain.ListResponse
import xyz.driver.pdsuidomain.entities._

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

  def nextStartAndEndPagesOption: (Option[Double], Option[Double]) =
    genBoundedRangeOption[Double](nextDouble(), nextDouble())

  def nextStartAndEndPages: (Double, Double) =
    genBoundedRange(nextDouble(), nextDouble())

  def nextPatientStatus: Patient.Status = generators.oneOf[Patient.Status](Patient.Status.All)

  def nextFullName[T]: FullName[T] = FullName(
    firstName = generators.nextName[T](10),
    middleName = generators.nextName[T](10),
    lastName = generators.nextName[T](10)
  )

  def nextCancerType: CancerType =
    generators.oneOf[CancerType](CancerType.Breast, CancerType.Lung, CancerType.Prostate)

  private val maxAttemptsNumber = 100

  def nextBridgeUploadQueueItem(): BridgeUploadQueue.Item = {
    BridgeUploadQueue.Item(
      kind = nextString(),
      tag = nextString(),
      created = nextLocalDateTime,
      attempts = nextInt(maxAttemptsNumber, minValue = 0),
      nextAttempt = nextLocalDateTime,
      completed = nextBoolean(),
      dependencyKind = nextOption(nextString()),
      dependencyTag = nextOption(nextString())
    )
  }

  def nextBridgeUploadQueueItemListResponse(): ListResponse[BridgeUploadQueue.Item] = {
    val xs: Seq[BridgeUploadQueue.Item] = Seq.fill(3)(nextBridgeUploadQueueItem())
    nextListResponse(xs)
  }

  def nextDocumentType(): DocumentType = generators.oneOf[DocumentType](DocumentType.All: _*)

  def nextProviderType(): ProviderType = generators.oneOf[ProviderType](ProviderType.All: _*)

  def nextDocumentTypeListResponse(): ListResponse[DocumentType] = {
    val xs: Seq[DocumentType] = Seq.fill(3)(nextDocumentType())
    nextListResponse(xs)
  }

  def nextProviderTypeListResponse(): ListResponse[ProviderType] = {
    val xs: Seq[ProviderType] = Seq.fill(3)(nextProviderType())
    nextListResponse(xs)
  }

  def nextTextJson[T](obj: T): TextJson[T] = TextJson(obj)

  def nextListResponse[T](xs: Seq[T]): ListResponse[T] = {
    val pageSize = generators.nextInt(xs.size, 1)
    ListResponse(
      items = xs,
      meta = ListResponse.Meta(
        itemsCount = xs.size,
        pageNumber = generators.nextInt(xs.size / pageSize),
        pageSize = pageSize,
        lastUpdate = generators.nextOption(nextLocalDateTime)
      )
    )
  }

}
