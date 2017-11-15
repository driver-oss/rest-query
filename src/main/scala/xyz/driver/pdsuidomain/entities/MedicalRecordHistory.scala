package xyz.driver.pdsuidomain.entities

import java.time.{LocalDateTime, ZoneId}

import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuidomain.entities.MedicalRecordHistory._

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
    case object Done     extends State
    case object Flag     extends State
    case object Archive  extends State

    val All: Set[State] = Set(
      State.New,
      State.Clean,
      State.Organize,
      State.Review,
      State.Done,
      State.Flag,
      State.Archive
    )

    private val stateToName: Map[State, String] =
      All.map(s => s -> s.toString).toMap

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
    case object Start           extends Action
    case object Submit          extends Action
    case object Unassign        extends Action
    case object Resolve         extends Action
    case object Flag            extends Action
    case object Archive         extends Action
    case object SaveDuplicate   extends Action
    case object SaveReorder     extends Action
    case object SaveRotation    extends Action
    case object DeleteDuplicate extends Action
    case object DeleteReorder   extends Action
    case object DeleteRotation  extends Action
    case object CreateDocument  extends Action
    case object DeleteDocument  extends Action
    case object CreateRecord    extends Action
    case object ReadRecord      extends Action

    val All: Set[Action] = Set(
      Action.Start,
      Action.Submit,
      Action.Unassign,
      Action.Resolve,
      Action.Flag,
      Action.Archive,
      Action.SaveDuplicate,
      Action.SaveReorder,
      Action.SaveRotation,
      Action.DeleteDuplicate,
      Action.DeleteReorder,
      Action.DeleteRotation,
      Action.CreateDocument,
      Action.DeleteDocument,
      Action.CreateRecord,
      Action.ReadRecord
    )

    private val actionToName: Map[Action, String] =
      All.map(a => a -> a.toString).toMap

    val fromString: PartialFunction[String, Action] =
      for ((k, v) <- actionToName) yield (v, k)

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
