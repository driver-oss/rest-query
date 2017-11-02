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
    phi"MedicalRecordHistory(id=$id, executor=$executor, recordId=$recordId, state=$state, action=$action, " +
      phi"created=$created)"
  }

  sealed trait State
  object State {
    case object Clean    extends State
    case object Organize extends State
    case object Review   extends State
    case object Flag     extends State

    val All: Set[State] = Set[State](Clean, Organize, Review, Flag)

    val fromString: PartialFunction[String, State] = {
      case "Clean"    => State.Clean
      case "Organize" => State.Organize
      case "Review"   => State.Review
      case "Flag"     => State.Flag
    }

    def stateToString(x: State): String = x match {
      case State.Clean    => "Clean"
      case State.Organize => "Organize"
      case State.Review   => "Review"
      case State.Flag     => "Flag"
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

final case class MedicalRecordHistory(id: LongId[MedicalRecordHistory],
                                      executor: StringId[User],
                                      recordId: LongId[MedicalRecord],
                                      state: State,
                                      action: Action,
                                      created: LocalDateTime = LocalDateTime.now(ZoneId.of("Z")))
