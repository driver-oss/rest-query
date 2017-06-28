package xyz.driver.pdsuidomain.formats.json.record

import xyz.driver.pdsuidomain.entities.MedicalRecord.Status

object MedicalRecordStatus {

  val statusFromString: PartialFunction[String, Status] = {
    case "Unprocessed" => Status.Unprocessed
    case "PreCleaning" => Status.PreCleaning
    case "New" => Status.New
    case "Cleaned" => Status.Cleaned
    case "PreOrganized" => Status.PreOrganized
    case "PreOrganizing" => Status.PreOrganizing
    case "Reviewed" => Status.Reviewed
    case "Organized" => Status.Organized
    case "Done" => Status.Done
    case "Flagged" => Status.Flagged
    case "Archived" => Status.Archived
  }

  def statusToString(x: Status): String = x match {
    case Status.Unprocessed => "Unprocessed"
    case Status.PreCleaning => "PreCleaning"
    case Status.New => "New"
    case Status.Cleaned => "Cleaned"
    case Status.PreOrganized => "PreOrganized"
    case Status.PreOrganizing => "PreOrganizing"
    case Status.Reviewed => "Reviewed"
    case Status.Organized => "Organized"
    case Status.Done => "Done"
    case Status.Flagged => "Flagged"
    case Status.Archived => "Archived"
  }
}
