package xyz.driver.pdsuicommon.concurrent

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue.Item
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueueRepositoryAdapter.Strategy
import xyz.driver.pdsuicommon.db._
import xyz.driver.pdsuicommon.db.repositories.BridgeUploadQueueRepository
import xyz.driver.pdsuicommon.logging._

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object BridgeUploadQueueRepositoryAdapter {

  /**
    * Defines how we work with queue, when an user attempts to remove/tryRetry an item.
    */
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
        Try(startInterval * Math.pow(intervalFactor, attempt.toDouble))
          .getOrElse(maxInterval)
      }
    }

    /**
      * Used only in tests.
      */
    final case class Stop(onComplete: OnComplete = OnComplete.Delete) extends Strategy {

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

class BridgeUploadQueueRepositoryAdapter(strategy: Strategy, repository: BridgeUploadQueueRepository, dbIo: DbIo)(
        implicit executionContext: ExecutionContext)
    extends BridgeUploadQueue with PhiLogging {

  override def add(item: Item): Future[Item] = dbIo.runAsync(repository.add(item))

  override def get(kind: String): Future[Option[Item]] = dbIo.runAsync(repository.getOne(kind))

  override def complete(kind: String, tag: String): Future[Unit] = {
    import Strategy.OnComplete._

    strategy.onComplete match {
      case Delete => dbIo.runAsync(repository.delete(kind, tag))
      case Mark =>
        dbIo.runAsyncTx {
          repository.getById(kind, tag) match {
            case Some(x) => repository.update(x.copy(completed = true))
            case None    => throw new RuntimeException(s"Can not find the task: kind=$kind, tag=$tag")
          }
        }
    }
  }

  /**
    * Tries to continue the task or complete it
    */
  override def tryRetry(item: Item): Future[Option[Item]] = {
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
        dbIo.runAsync {
          Some(repository.update(draftItem))
        }

      case Complete =>
        logger.warn(phi"All attempts are out for $item, complete the task")
        complete(item.kind, item.tag).map(_ => None)
    }
  }
}
