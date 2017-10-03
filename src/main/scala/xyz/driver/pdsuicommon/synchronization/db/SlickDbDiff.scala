package xyz.driver.pdsuicommon.synchronization.db

import xyz.driver.pdsuicommon.synchronization.domain.FakeId
import xyz.driver.pdsuicommon.synchronization.utils.{FakeIdGen, Refiner}

import scala.annotation.tailrec
import scala.collection.breakOut
import scala.collection.immutable.SortedSet

object SlickDbDiff {

  /**
    * Calculates DB-actions to synchronize origEntities with draftEntities.
    */
  def calc[DraftT, OrigT](origEntities: Iterable[OrigT], draftEntities: Iterable[DraftT])(
          implicit draftFakeIdGen: FakeIdGen[DraftT],
          origFakeIdGen: FakeIdGen[OrigT],
          refiner: Refiner[DraftT, OrigT]): List[SlickDbAction[OrigT]] = {
    val origMap: Map[FakeId, OrigT] = origEntities.map(x => origFakeIdGen(x) -> x)(breakOut)
    val uniqueDraftEntities         = SortedSet.newBuilder[DraftT](Ordering.by[DraftT, FakeId](draftFakeIdGen))
    uniqueDraftEntities ++= draftEntities

    loop(origMap, uniqueDraftEntities.result(), List.empty)
  }

  @tailrec private def loop[DraftT, OrigT](origEntitiesMap: Map[FakeId, OrigT],
                                           draftEntities: Iterable[DraftT],
                                           actions: List[SlickDbAction[OrigT]])(
          implicit draftFakeIdGen: FakeIdGen[DraftT],
          refiner: Refiner[DraftT, OrigT]): List[SlickDbAction[OrigT]] = {
    draftEntities.headOption match {
      case None =>
        // The rest original entities are not a part of draft, so we will delete them
        val toDelete: List[SlickDbAction[OrigT]] = origEntitiesMap.values.map(x => SlickDbAction.Delete(x))(breakOut)
        actions ++ toDelete

      case Some(currRaw) =>
        val rawCore = draftFakeIdGen.getFor(currRaw)
        val action: Option[SlickDbAction[OrigT]] = origEntitiesMap.get(rawCore) match {
          // It is a new entity, because it doesn't exist among originals
          case None => Some(SlickDbAction.Create(refiner.refine(currRaw)))
          case Some(orig) =>
            val draft = refiner.refresh(orig, currRaw)
            if (draft == orig) None
            else Some(SlickDbAction.Update(draft))
        }

        loop(origEntitiesMap - rawCore, draftEntities.tail, action.map(_ :: actions).getOrElse(actions))
    }
  }

}
