package xyz.driver.pdsuidomain.entities

import java.time.{LocalDateTime, ZoneId}

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils

final case class UserHistory(id: LongId[UserHistory],
                             executor: StringId[User],
                             recordId: Option[LongId[MedicalRecord]] = None,
                             documentId: Option[LongId[Document]] = None,
                             trialId: Option[StringId[Trial]] = None,
                             patientId: Option[UuidId[Patient]] = None,
                             state: UserHistory.State,
                             action: UserHistory.Action,
                             created: LocalDateTime = LocalDateTime.now(ZoneId.of("Z")))

object UserHistory {

  def forDocument(executor: StringId[User],
                  documentId: LongId[Document],
                  state: UserHistory.State,
                  action: UserHistory.Action): UserHistory = UserHistory(
    id = LongId(0L),
    executor = executor,
    documentId = Some(documentId),
    state = state,
    action = action
  )

  def forRecord(executor: StringId[User],
                recordId: LongId[MedicalRecord],
                state: UserHistory.State,
                action: UserHistory.Action): UserHistory = UserHistory(
    id = LongId(0L),
    executor = executor,
    recordId = Some(recordId),
    state = state,
    action = action
  )

  def forPatient(executor: StringId[User],
                 patientId: UuidId[Patient],
                 state: UserHistory.State,
                 action: UserHistory.Action): UserHistory = UserHistory(
    id = LongId(0L),
    executor = executor,
    patientId = Some(patientId),
    state = state,
    action = action
  )

  sealed trait State extends Product with Serializable {

    def oneOf(xs: State*): Boolean = xs.contains(this)

    def oneOf(xs: Set[State]): Boolean = xs.contains(this)
  }

  object State {
    case object Clean        extends State
    case object Organize     extends State
    case object Extract      extends State
    case object Summarize    extends State
    case object Criteriarize extends State
    case object Verify       extends State
    case object Curate       extends State
    case object Review       extends State
    case object Flag         extends State

    val All: Set[State] = Set[State](Clean, Organize, Extract, Summarize, Criteriarize, Verify, Curate, Review, Flag)

    val fromString: PartialFunction[String, State] = {
      case "Clean"        => State.Clean
      case "Organize"     => State.Organize
      case "Extract"      => State.Extract
      case "Summarize"    => State.Summarize
      case "Criteriarize" => State.Criteriarize
      case "Verify"       => State.Verify
      case "Curate"       => State.Curate
      case "Review"       => State.Review
      case "Flag"         => State.Flag
    }

    def stateToString(x: State): String = x match {
      case State.Clean        => "Clean"
      case State.Organize     => "Organize"
      case State.Extract      => "Extract"
      case State.Summarize    => "Summarize"
      case State.Criteriarize => "Criteriarize"
      case State.Verify       => "Verify"
      case State.Curate       => "Curate"
      case State.Review       => "Review"
      case State.Flag         => "Flag"
    }

    implicit def toPhiString(x: State): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
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

    val All: Set[Action] = Set[Action](Start, Submit, Unassign, Resolve, Flag, Archive)

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

    implicit def toPhiString(x: Action): PhiString = Unsafe(Utils.getClassSimpleName(x.getClass))
  }

  implicit def toPhiString(x: UserHistory): PhiString = {
    import x._
    phi"UserHistory(id=$id, executor=$executor, recordId=$recordId, " +
      phi"documentId=$documentId, trialId=$trialId, patientId=$patientId, " +
      phi"state=$state, action=$action, created=$created)"
  }
}
