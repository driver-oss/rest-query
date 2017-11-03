package xyz.driver.pdsuidomain.entities

import java.time.{LocalDateTime, ZoneId}

import xyz.driver.core.auth.User
import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import xyz.driver.pdsuidomain.entities.DocumentHistory._

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

    val All: Set[State] = Set[State](Extract, Review, Flag)

    val fromString: PartialFunction[String, State] = {
      case "Extract" => State.Extract
      case "Review"  => State.Review
      case "Flag"    => State.Flag
    }

    def stateToString(x: State): String = x match {
      case State.Extract => "Extract"
      case State.Review  => "Review"
      case State.Flag    => "Flag"
    }

    implicit def toPhiString(x: State): PhiString =
      Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  sealed trait Action extends Product with Serializable {

    def oneOf(xs: Action*): Boolean = xs.contains(this)

    def oneOf(xs: Set[Action]): Boolean = xs.contains(this)

  }

  object Action {
    case object Start    extends Action
    case object Submit   extends Action
    case object Unassign extends Action
    case object Resolve  extends Action
    case object Flag     extends Action
    case object Archive  extends Action

    val All: Set[Action] =
      Set[Action](Start, Submit, Unassign, Resolve, Flag, Archive)

    val fromString: PartialFunction[String, Action] = {
      case "Start"    => Action.Start
      case "Submit"   => Action.Submit
      case "Unassign" => Action.Unassign
      case "Resolve"  => Action.Resolve
      case "Flag"     => Action.Flag
      case "Archive"  => Action.Archive
    }

    def actionToString(x: Action): String = x match {
      case Action.Start    => "Start"
      case Action.Submit   => "Submit"
      case Action.Unassign => "Unassign"
      case Action.Resolve  => "Resolve"
      case Action.Flag     => "Flag"
      case Action.Archive  => "Archive"
    }

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
