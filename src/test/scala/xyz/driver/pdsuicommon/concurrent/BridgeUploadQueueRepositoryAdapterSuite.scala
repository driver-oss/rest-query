package xyz.driver.pdsuicommon.concurrent

import java.util.concurrent.ThreadLocalRandom

import xyz.driver.pdsuicommon.BaseSuite
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueueRepositoryAdapter.Strategy
import xyz.driver.pdsuicommon.concurrent.BridgeUploadQueueRepositoryAdapter.Strategy.{OnAttempt, OnComplete}
import xyz.driver.pdsuicommon.db.{FakeDbIo, MysqlQueryBuilder}
import xyz.driver.pdsuicommon.db.repositories.BridgeUploadQueueRepository

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class BridgeUploadQueueRepositoryAdapterSuite extends BaseSuite {

  // IDEA have some issue here with imports
  private implicit val executionContext = scala.concurrent.ExecutionContext.global

  "Strategy" - {
    "LimitExponential" - {
      "on" - {
        val strategy = Strategy.LimitExponential(
          startInterval = 10.seconds,
          intervalFactor = 1.4,
          maxInterval = 50.seconds,
          onComplete = OnComplete.Delete
        )

        "a new interval should be greater than the previous one if the limit not reached" in {
          val previous = strategy.on(1)
          val current  = strategy.on(2)

          (previous, current) match {
            case (OnAttempt.Continue(a), OnAttempt.Continue(b)) => assert(a < b)
            case x                                              => fail(s"Unexpected result: $x")
          }
        }

        "should limit intervals" in {
          assert(strategy.on(20) == OnAttempt.Continue(strategy.maxInterval))
        }

        "should not fail, if there is many attempts" in {
          assert(strategy.on(1000) == OnAttempt.Continue(strategy.maxInterval))
        }
      }
    }
  }

  "complete" - {
    "onComplete == mark" - {
      "should update the item" in {
        var done = false
        val item = defaultItem

        val repository = new BridgeUploadQueueRepository {
          override def add(draft: EntityT): EntityT           = draft
          override def getOne(kind: String): Option[EntityT]  = fail("getOne should not be used!")
          override def buildQuery: MysqlQueryBuilder[EntityT] = fail("buildQuery should not be used!")

          override def delete(kind: String, tag: String): Unit = throw new IllegalStateException("Impossible call")

          override def update(entity: EntityT): EntityT = {
            assert(entity.kind == item.kind, "repository.delete, kind")
            assert(entity.tag == item.tag, "repository.delete, tag")
            done = true
            entity
          }

          override def getById(kind: String, tag: String): Option[EntityT] = Some(item)
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = Strategy.Stop(OnComplete.Mark),
          repository = repository,
          dbIo = FakeDbIo
        )

        assert(adapter.complete(item.kind, item.tag).isReadyWithin(100.millis))
        assert(done)
      }
    }

    "onComplete == delete" - {
      "should delete the item" in {
        var done = false
        val item = defaultItem

        val repository = new BridgeUploadQueueRepository {
          override def add(draft: EntityT): EntityT                        = draft
          override def getOne(kind: String): Option[EntityT]               = fail("getOne should not be used!")
          override def buildQuery: MysqlQueryBuilder[EntityT]              = fail("buildQuery should not be used!")
          override def getById(kind: String, tag: String): Option[EntityT] = fail("getById should not be used!")

          override def delete(kind: String, tag: String): Unit = {
            assert(kind == item.kind, "repository.delete, kind")
            assert(tag == item.tag, "repository.delete, tag")
            done = true
          }
          override def update(entity: EntityT): EntityT = throw new IllegalStateException("Impossible call")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = Strategy.Stop(OnComplete.Delete),
          repository = repository,
          dbIo = FakeDbIo
        )

        assert(adapter.complete(item.kind, item.tag).isReadyWithin(100.millis))
        assert(done)
      }
    }
  }

  "tryRetry" - {

    "when all attempts are not out" - {

      val defaultStrategy = Strategy.Constant(10.seconds)

      "should return an updated item" in {
        val repository = new BridgeUploadQueueRepository {
          override def add(draft: EntityT): EntityT                        = draft
          override def getOne(kind: String): Option[EntityT]               = fail("getOne should not be used!")
          override def buildQuery: MysqlQueryBuilder[EntityT]              = fail("buildQuery should not be used!")
          override def getById(kind: String, tag: String): Option[EntityT] = fail("getById should not be used!")

          override def update(draft: EntityT): EntityT         = draft
          override def delete(kind: String, tag: String): Unit = throw new IllegalAccessError(s"kind=$kind, tag=$tag")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          dbIo = FakeDbIo
        )

        val item = defaultItem
        val r    = adapter.tryRetry(item).futureValue
        assert(r.isDefined)
        assert(!r.contains(item))
      }

      "should update an item with increased attempts" in {
        val item = defaultItem

        val repository = new BridgeUploadQueueRepository {
          override def add(draft: EntityT): EntityT                        = draft
          override def getOne(kind: String): Option[EntityT]               = fail("getOne should not be used!")
          override def buildQuery: MysqlQueryBuilder[EntityT]              = fail("buildQuery should not be used!")
          override def getById(kind: String, tag: String): Option[EntityT] = fail("getById should not be used!")

          override def update(draft: EntityT): EntityT = {
            assert(draft.attempts === (item.attempts + 1), "repository.add")
            draft
          }
          override def delete(kind: String, tag: String): Unit = throw new IllegalAccessError(s"kind=$kind, tag=$tag")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          dbIo = FakeDbIo
        )

        assert(adapter.tryRetry(item).isReadyWithin(100.millis))
      }

      "should remove an old item" in {
        val item = defaultItem

        val repository = new BridgeUploadQueueRepository {
          override def add(draft: EntityT): EntityT                        = draft
          override def getOne(kind: String): Option[EntityT]               = fail("getOne should not be used!")
          override def buildQuery: MysqlQueryBuilder[EntityT]              = fail("buildQuery should not be used!")
          override def getById(kind: String, tag: String): Option[EntityT] = fail("getById should not be used!")
          override def update(draft: EntityT): EntityT                     = draft
          override def delete(kind: String, tag: String): Unit = {
            assert(kind == item.kind, "repository.delete, kind")
            assert(tag == item.tag, "repository.delete, kind")
          }
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          dbIo = FakeDbIo
        )

        assert(adapter.tryRetry(item).isReadyWithin(100.millis))
      }

      "should update time of the next attempt" in {
        val item = defaultItem

        val repository = new BridgeUploadQueueRepository {
          override def add(draft: EntityT): EntityT                        = draft
          override def getOne(kind: String): Option[EntityT]               = fail("getOne should not be used!")
          override def buildQuery: MysqlQueryBuilder[EntityT]              = fail("buildQuery should not be used!")
          override def getById(kind: String, tag: String): Option[EntityT] = fail("getById should not be used!")

          override def update(draft: EntityT): EntityT = {
            assert(draft.nextAttempt.isAfter(item.nextAttempt), "repository.add")
            draft
          }
          override def delete(kind: String, tag: String): Unit = throw new IllegalAccessError(s"kind=$kind, tag=$tag")
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          dbIo = FakeDbIo
        )

        assert(adapter.tryRetry(item).isReadyWithin(100.millis))
      }

    }

    "when all attempts are out" - {

      val defaultStrategy = Strategy.Stop()

      "should not return an item" in {
        val repository = new BridgeUploadQueueRepository {
          override def add(draft: EntityT): EntityT                        = draft
          override def getOne(kind: String): Option[EntityT]               = fail("getOne should not be used!")
          override def buildQuery: MysqlQueryBuilder[EntityT]              = fail("buildQuery should not be used!")
          override def getById(kind: String, tag: String): Option[EntityT] = fail("getById should not be used!")
          override def update(entity: EntityT): EntityT                    = fail("update should not be used!")

          override def delete(kind: String, tag: String): Unit = {}
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          dbIo = FakeDbIo
        )

        val r = adapter.tryRetry(defaultItem).futureValue
        assert(r.isEmpty)
      }

      "should complete the item" in {
        var taskWasCompleted = false
        val item             = defaultItem

        val repository = new BridgeUploadQueueRepository {
          override def add(draft: EntityT): EntityT                        = draft
          override def getOne(kind: String): Option[EntityT]               = fail("getOne should not be used!")
          override def buildQuery: MysqlQueryBuilder[EntityT]              = fail("buildQuery should not be used!")
          override def getById(kind: String, tag: String): Option[EntityT] = fail("getById should not be used!")
          override def update(entity: EntityT): EntityT                    = fail("update should not be used!")

          override def delete(kind: String, tag: String): Unit = {}
        }

        val adapter = new BridgeUploadQueueRepositoryAdapter(
          strategy = defaultStrategy,
          repository = repository,
          dbIo = FakeDbIo
        ) {
          override def complete(kind: String, tag: String): Future[Unit] = Future {
            assert(kind == item.kind, "adapter.complete, kind")
            assert(tag == item.tag, "adapter.complete, tag")
            taskWasCompleted = true
          }
        }

        val r = adapter.tryRetry(item).futureValue
        assert(r.isEmpty)
        assert(taskWasCompleted)
      }

    }

  }

  private def defaultItem = BridgeUploadQueue.Item(
    "test",
    ThreadLocalRandom.current().nextInt().toString
  )

}
