package xyz.driver.pdsuidomain.formats.json.trial

import xyz.driver.pdsuidomain.entities.Trial.Status

object TrialStatus {

  val statusFromString: PartialFunction[String, Status] = {
    case "New" => Status.New
    case "ReviewSummary" => Status.ReviewSummary
    case "Summarized" => Status.Summarized
    case "PendingUpdate" => Status.PendingUpdate
    case "Update" => Status.Update
    case "ReviewCriteria" => Status.ReviewCriteria
    case "Done" => Status.Done
    case "Flagged" => Status.Flagged
    case "Archived" => Status.Archived
  }

  def statusToString(x: Status): String = x match {
    case Status.New => "New"
    case Status.ReviewSummary => "ReviewSummary"
    case Status.Summarized => "Summarized"
    case Status.PendingUpdate => "PendingUpdate"
    case Status.Update => "Update"
    case Status.ReviewCriteria => "ReviewCriteria"
    case Status.Done => "Done"
    case Status.Flagged => "Flagged"
    case Status.Archived => "Archived"
  }
}
