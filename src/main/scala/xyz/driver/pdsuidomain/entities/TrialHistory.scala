package xyz.driver.pdsuidomain.entities

import java.time.{LocalDateTime, ZoneId}

import xyz.driver.pdsuicommon.domain._
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuicommon.utils.Utils
import TrialHistory._
import xyz.driver.core.auth.User

object TrialHistory {

  implicit def toPhiString(x: TrialHistory): PhiString = {
    import x._
    phi"TrialHistory(id=$id, executor=${Unsafe(executor)}, trialId=$trialId, state=$state, action=$action, created=$created)"
  }

  sealed trait State
  object State {
    case object Summarize      extends State
    case object Criteriarize   extends State
    case object ReviewSummary  extends State
    case object ReviewCriteria extends State
    case object Flag           extends State

    val All: Set[State] = Set[State](Summarize, Criteriarize, ReviewSummary, ReviewCriteria, Flag)

    val fromString: PartialFunction[String, State] = {
      case "Summarize"      => State.Summarize
      case "Criteriarize"   => State.Criteriarize
      case "ReviewSummary"  => State.ReviewSummary
      case "ReviewCriteria" => State.ReviewCriteria
      case "Flag"           => State.Flag
    }

    def stateToString(x: State): String = x match {
      case State.Summarize      => "Summarize"
      case State.Criteriarize   => "Criteriarize"
      case State.ReviewSummary  => "ReviewSummary"
      case State.ReviewCriteria => "ReviewCriteria"
      case State.Flag           => "Flag"
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

final case class TrialHistory(id: LongId[TrialHistory],
                              executor: xyz.driver.core.Id[User],
                              trialId: StringId[Trial],
                              state: State,
                              action: Action,
                              created: LocalDateTime = LocalDateTime.now(ZoneId.of("Z")),
                              comment: Option[String] = None)
