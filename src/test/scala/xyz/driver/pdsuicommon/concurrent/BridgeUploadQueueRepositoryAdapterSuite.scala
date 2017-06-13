package xyz.driver.pdsuicommon.concurrent

import java.util.concurrent.ThreadLocalRandom

import xyz.driver.pdsuicommon.BaseSuite
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueue.Item
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueueRepositoryAdapter.Strategy
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueueRepositoryAdapter.Strategy.{OnAttempt, OnComplete}
import xyz.driver.pdsuicommon.db.repositories.BridgeUploadQueueRepository
import xyz.driver.pdsuicommon.domain.LongId

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class BridgeUploadQueueRepositoryAdapterSuite extends BaseSuite {

  // IDEA have some issue here with imports
  private implicit val executionContext = scala.concurrent.ExecutionContext.global

  "Strategy" - {
    "LimitExponential" - {
      "calculateNextInterval" - {
        val strategy = Strategy.LimitExponential(
          startInterval = 10.seconds,
          intervalFactor = 1.4,
          maxInterval = 50.seconds,
          onComplete = OnComplete.Delete
        )

        "a new interval should be greater than the previous one if the limit not reached" in {
          val previous = strategy.on(1)
          val current = strategy.on(2)

          (previous, current) match {
            case (OnAttempt.Continue(a), OnAttempt.Continue(b)) => assert(a < b)
            case x => fail(s"Unexpected result: $x")
          }
        }

        "should limit intervals" in {
          assert(strategy.on(20) == OnAttempt.Continue(strategy.maxInterval))
        }
      }
    }
  }

  "tryRetry" - {

    "when all attempts are not out" - {

      val defaultStrategy = Strategy.Constant(10.seconds)

      "should return an updated item" in {
        val repository = new BridgeUploadQueueRepository {
          override def update(draft: EntityT): EntityT = draft
          override def delete(id: IdT): Unit = {}
          override def add(draft: EntityT): EntityT = fail("add should not be used!")
          override def getById(id: LongId[EntityT]): Option[EntityT] = fail("getById should not be used!")
          override def isCompleted(kind: String, tag: String): Future[Boolean] = fail("isCompleted should not be used!")
          override def getOne(kind: String): Future[Option[Item]] = fail("getOne should not be used!")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          transactions = transactions
        )

        val item = defaultItem
        val r = adapter.tryRetry(item).futureValue
        assert(r.isDefined)
        assert(!r.contains(item))
      }

      "should add an item with increased attempts" in {
        val item = defaultItem

        val repository = new BridgeUploadQueueRepository {
          override def update(draft: EntityT): EntityT = {
            assert(draft.attempts === (item.attempts + 1), "repository.add")
            draft
          }
          override def delete(id: IdT): Unit = {}
          override def add(draft: EntityT): EntityT = fail("add should not be used!")
          override def getById(id: LongId[EntityT]): Option[EntityT] = fail("getById should not be used!")
          override def isCompleted(kind: String, tag: String): Future[Boolean] = fail("isCompleted should not be used!")
          override def getOne(kind: String): Future[Option[Item]] = fail("getOne should not be used!")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          transactions = transactions
        )

        adapter.tryRetry(item).isReadyWithin(100.millis)
      }

      "should remove an old item" in {
        val item = defaultItem

        val repository = new BridgeUploadQueueRepository {
          override def update(draft: EntityT): EntityT = draft
          override def delete(id: IdT): Unit = {
            assert(id == item.id, "repository.delete")
          }
          override def add(draft: EntityT): EntityT = fail("add should not be used!")
          override def getById(id: LongId[EntityT]): Option[EntityT] = fail("getById should not be used!")
          override def isCompleted(kind: String, tag: String): Future[Boolean] = fail("isCompleted should not be used!")
          override def getOne(kind: String): Future[Option[Item]] = fail("getOne should not be used!")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          transactions = transactions
        )

        adapter.tryRetry(item).isReadyWithin(100.millis)
      }

      "should update time of the next attempt" in {
        val item = defaultItem

        val repository = new BridgeUploadQueueRepository {
          override def update(draft: EntityT): EntityT = {
            assert(draft.nextAttempt.isAfter(item.nextAttempt), "repository.add")
            draft
          }
          override def delete(id: IdT): Unit = {}
          override def add(draft: EntityT): EntityT = fail("add should not be used!")
          override def getById(id: LongId[EntityT]): Option[EntityT] = fail("getById should not be used!")
          override def isCompleted(kind: String, tag: String): Future[Boolean] = fail("isCompleted should not be used!")
          override def getOne(kind: String): Future[Option[Item]] = fail("getOne should not be used!")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          transactions = transactions
        )

        adapter.tryRetry(item).isReadyWithin(100.millis)
      }

    }

    "when all attempts are out" - {

      val defaultStrategy = Strategy.Ignore

      "should not return an item" in {
        val repository = new BridgeUploadQueueRepository {
          override def delete(id: IdT): Unit = {}
          override def update(entity: EntityT): EntityT = fail("update should not be used!")
          override def add(draft: EntityT): EntityT = fail("add should not be used!")
          override def getById(id: LongId[EntityT]): Option[EntityT] = fail("getById should not be used!")
          override def isCompleted(kind: String, tag: String): Future[Boolean] = fail("isCompleted should not be used!")
          override def getOne(kind: String): Future[Option[Item]] = fail("getOne should not be used!")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          transactions = transactions
        )

        val r = adapter.tryRetry(defaultItem).futureValue
        assert(r.isEmpty)
      }

      "should not add any item to the queue" in {
        val repository = new BridgeUploadQueueRepository {
          override def update(draft: EntityT): EntityT = throw new IllegalAccessException("add should not be called")
          override def delete(id: IdT): Unit = {}
          override def add(draft: EntityT): EntityT = fail("add should not be used!")
          override def getById(id: LongId[EntityT]): Option[EntityT] = fail("getById should not be used!")
          override def isCompleted(kind: String, tag: String): Future[Boolean] = fail("isCompleted should not be used!")
          override def getOne(kind: String): Future[Option[Item]] = fail("getOne should not be used!")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          transactions = transactions
        )

        adapter.tryRetry(defaultItem).isReadyWithin(100.millis)
      }

      "should remove the item from the queue" in {
        val repository = new BridgeUploadQueueRepository {
          override def delete(id: IdT): Unit = {
            assert(id == defaultItem.id, "repository.delete")
          }
          override def update(entity: EntityT): EntityT = fail("update should not be used!")
          override def add(draft: EntityT): EntityT = fail("add should not be used!")
          override def getById(id: LongId[EntityT]): Option[EntityT] = fail("getById should not be used!")
          override def isCompleted(kind: String, tag: String): Future[Boolean] = fail("isCompleted should not be used!")
          override def getOne(kind: String): Future[Option[Item]] = fail("getOne should not be used!")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          transactions = transactions
        )

        adapter.tryRetry(defaultItem).isReadyWithin(100.millis)
      }

    }

  }

  private def defaultItem = BridgeUploadQueue.Item(
    "test",
    ThreadLocalRandom.current().nextInt().toString
  )

}
