package xyz.driver.pdsuicommon.concurrent

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue.Item
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueueRepositoryAdapter.Strategy
import xyz.driver.pdsuicommon.db.Transactions
import xyz.driver.pdsuicommon.db.repositories.BridgeUploadQueueRepository
import xyz.driver.pdsuicommon.domain.LongId
import xyz.driver.pdsuicommon.logging._

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

object BridgeUploadQueueRepositoryAdapter {

  sealed trait Strategy {

    def onComplete: Strategy.OnComplete

    def on(attempt: Int): Strategy.OnAttempt

  }

  object Strategy {

    /**
      * Works forever, but has a limit for intervals.
      */
    final case class LimitExponential(startInterval: FiniteDuration,
                                      intervalFactor: Double,
                                      maxInterval: FiniteDuration,
                                      onComplete: OnComplete)
        extends Strategy {

      override def on(attempt: Int): OnAttempt = {
        OnAttempt.Continue(intervalFor(attempt).min(maxInterval))
      }

      private def intervalFor(attempt: Int): Duration = {
        startInterval * Math.pow(intervalFactor, attempt.toDouble)
      }
    }

    /**
      * Used only in tests.
      */
    case object Ignore extends Strategy {

      override val onComplete = OnComplete.Delete

      override def on(attempt: Int) = OnAttempt.Complete

    }

    /**
      * Used only in tests.
      */
    final case class Constant(interval: FiniteDuration) extends Strategy {

      override val onComplete = OnComplete.Delete

      override def on(attempt: Int) = OnAttempt.Continue(interval)

    }

    sealed trait OnComplete
    object OnComplete {
      case object Delete extends OnComplete
      case object Mark   extends OnComplete

      implicit def toPhiString(x: OnAttempt): PhiString = Unsafe(x.toString)
    }

    sealed trait OnAttempt
    object OnAttempt {
      case object Complete                    extends OnAttempt
      case class Continue(interval: Duration) extends OnAttempt

      implicit def toPhiString(x: OnAttempt): PhiString = Unsafe(x.toString)
    }
  }
}

class BridgeUploadQueueRepositoryAdapter(strategy: Strategy,
                                         repository: BridgeUploadQueueRepository,
                                         transactions: Transactions)(implicit executionContext: ExecutionContext)
    extends BridgeUploadQueue with PhiLogging {

  override def add(item: Item): Future[Unit] = transactions.run { _ =>
    repository.add(item)
  }

  override def get(kind: String): Future[Option[Item]] = {
    repository.getOne(kind)
  }

  override def remove(item: LongId[Item]): Future[Unit] = transactions.run { _ =>
    import Strategy.OnComplete._

    strategy.onComplete match {
      case Delete => repository.delete(item)
      case Mark =>
        repository.getById(item) match {
          case Some(x) => repository.update(x.copy(completed = true))
          case None    => throw new RuntimeException(s"Can not find the $item task")
        }
    }
  }

  override def tryRetry(item: Item): Future[Option[Item]] = transactions.run { _ =>
    import Strategy.OnAttempt._

    logger.trace(phi"tryRetry($item)")

    val newAttempts = item.attempts + 1
    val action      = strategy.on(newAttempts)
    logger.debug(phi"Action for ${Unsafe(newAttempts)}: $action")

    action match {
      case Continue(newInterval) =>
        val draftItem = item.copy(
          attempts = newAttempts,
          nextAttempt = LocalDateTime.now().plus(newInterval.toMillis, ChronoUnit.MILLIS)
        )

        logger.debug(draftItem)
        Some(repository.update(draftItem))

      case Complete =>
        repository.delete(item.id)
        None
    }
  }
}
