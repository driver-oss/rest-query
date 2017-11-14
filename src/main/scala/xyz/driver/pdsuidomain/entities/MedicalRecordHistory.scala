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
    case object Clean    extends State
    case object Organize extends State
    case object Review   extends State
    case object Flag     extends State

    private val stateToName = immutable.Map[State, String](
      State.Clean    -> "Clean",
      State.Organize -> "Organize",
      State.Review   -> "Review",
      State.Flag     -> "Flag"
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

    private val actionToName = immutable.Map[Action, String](
      Action.Start            -> "Start",
      Action.Submit           -> "Submit",
      Action.Unassign         -> "Unassign",
      Action.Resolve          -> "Resolve",
      Action.Flag             -> "Flag",
      Action.Archive          -> "Archive",
      Action.SavedDuplicate   -> "SavedDuplicate",
      Action.SavedReorder     -> "SavedReorder",
      Action.SavedRotation    -> "SavedRotate",
      Action.DeletedDuplicate -> "DeletedDuplicate",
      Action.DeletedReorder   -> "DeletedReorder",
      Action.DeletedRotation  -> "DeletedRotation",
      Action.CreatedDocument  -> "CreatedDocument",
      Action.DeletedDocument  -> "DeletedDocument",
      Action.CreatedRecord    -> "CreatedRecord",
      Action.ReadRecord       -> "ReadRecord"
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
