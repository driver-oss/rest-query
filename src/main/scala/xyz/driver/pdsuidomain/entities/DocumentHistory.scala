package xyz.driver.pdsuidomain.entities

import java.time.{LocalDateTime, ZoneId}

import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuidomain.entities.DocumentHistory.{Action, _}

import scala.collection.immutable

object DocumentHistory {

  implicit def toPhiString(x: DocumentHistory): PhiString = {
    import x._
    phi"DocumentHistory(id=$id, executor=${Unsafe(executor)}, documentId=$documentId, state=$state, action=$action, " +
      phi"created=$created)"
  }

  sealed trait State
  object State {
    case object Extract extends State
    case object Review  extends State
    case object Flag    extends State
    case object New     extends State

    private implicit def stateToName(state: State): (State, String) = {
      state -> state.toString
    }

    private val stateToName = immutable.Map[State, String](
      State.Extract,
      State.Review,
      State.Flag,
      State.New
    )

    val All: Set[State] = stateToName.keySet

    val fromString: PartialFunction[String, State] =
      for ((k, v) <- stateToName) yield (v, k)

    def stateToString: State => String = stateToName

    implicit def toPhiString(x: State): PhiString =
      Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  sealed trait Action extends Product with Serializable {

    def oneOf(xs: Action*): Boolean = xs.contains(this)

    def oneOf(xs: Set[Action]): Boolean = xs.contains(this)

  }

  object Action {
    case object Start           extends Action
    case object Submit          extends Action
    case object Unassign        extends Action
    case object Resolve         extends Action
    case object Flag            extends Action
    case object Archive         extends Action
    case object PostedEvidence  extends Action
    case object CreatedDocument extends Action
    case object ReadDocument    extends Action

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
      Action.PostedEvidence,
      Action.CreatedDocument,
      Action.ReadDocument
    )

    val All: Set[Action] = actionToName.keySet

    val fromString: PartialFunction[String, Action] =
      for ((k, v) <- actionToName) yield (v, k)

    def actionToString: Action => String = actionToName

    implicit def toPhiString(x: Action): PhiString =
      Unsafe(Utils.getClassSimpleName(x.getClass))
  }
}

final case class DocumentHistory(id: LongId[DocumentHistory],
                                 executor: xyz.driver.core.Id[User],
                                 documentId: LongId[Document],
                                 state: State,
                                 action: Action,
                                 created: LocalDateTime = LocalDateTime.now(ZoneId.of("Z")))
