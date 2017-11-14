package xyz.driver.pdsuidomain.entities

import java.time.{LocalDateTime, ZoneId}

import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuidomain.entities.MedicalRecordHistory._

import scala.collection.immutable

object MedicalRecordHistory {

  implicit def toPhiString(x: MedicalRecordHistory): PhiString = {
    import x._
    phi"MedicalRecordHistory(id=$id, executor=${Unsafe(executor)}, recordId=$recordId, state=$state, action=$action, " +
      phi"created=$created)"
  }

  sealed trait State

  object State {
    case object New      extends State
    case object Clean    extends State
    case object Organize extends State
    case object Review   extends State
    case object Flag     extends State

    private implicit def stateToName(state: State): (State, String) = {
      state -> state.toString
    }

    private val stateToName = immutable.Map[State, String](
      State.New,
      State.Clean,
      State.Organize,
      State.Review,
      State.Flag
    )

    val All: Set[State] = stateToName.keySet

    val fromString: PartialFunction[String, State] =
      for ((k, v) <- stateToName) yield (v, k)

    def stateToString: State => String = stateToName

    implicit def toPhiString(x: State): PhiString =
      Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  sealed trait Action extends Product with Serializable {
    def oneOf(xs: Action*): Boolean     = xs.contains(this)
    def oneOf(xs: Set[Action]): Boolean = xs.contains(this)
  }

  object Action {
    case object Start            extends Action
    case object Submit           extends Action
    case object Unassign         extends Action
    case object Resolve          extends Action
    case object Flag             extends Action
    case object Archive          extends Action
    case object SavedDuplicate   extends Action
    case object SavedReorder     extends Action
    case object SavedRotation    extends Action
    case object DeletedDuplicate extends Action
    case object DeletedReorder   extends Action
    case object DeletedRotation  extends Action
    case object CreatedDocument  extends Action
    case object DeletedDocument  extends Action
    case object CreatedRecord    extends Action
    case object ReadRecord       extends Action

    private implicit def stateToName(action: Action): (Action, String) = {
      action -> action.toString
    }

    private val actionToName = immutable.Map[Action, String](
      Action.Start,
      Action.Submit,
      Action.Unassign,
      Action.Resolve,
      Action.Flag,
      Action.Archive,
      Action.SavedDuplicate,
      Action.SavedReorder,
      Action.SavedRotation,
      Action.DeletedDuplicate,
      Action.DeletedReorder,
      Action.DeletedRotation,
      Action.CreatedDocument,
      Action.DeletedDocument,
      Action.CreatedRecord,
      Action.ReadRecord
    )

    val fromString: PartialFunction[String, Action] =
      for ((k, v) <- actionToName) yield (v, k)

    val All: Set[Action] = actionToName.keySet

    def actionToString: Action => String = actionToName

    implicit def toPhiString(x: Action): PhiString =
      Unsafe(Utils.getClassSimpleName(x.getClass))
  }
}

final case class MedicalRecordHistory(id: LongId[MedicalRecordHistory],
                                      executor: xyz.driver.core.Id[User],
                                      recordId: LongId[MedicalRecord],
                                      state: State,
                                      action: Action,
                                      created: LocalDateTime = LocalDateTime.now(ZoneId.of("Z")))
