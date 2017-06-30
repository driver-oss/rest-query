package xyz.driver.pdsuidomain.formats.json.patient

import xyz.driver.pdsuidomain.entities.Patient.Status

object PatientStatus {

  val statusFromString: PartialFunction[String, Status] = {
    case "New"      => Status.New
    case "Verified" => Status.Verified
    case "Reviewed" => Status.Reviewed
    case "Curated"  => Status.Curated
    case "Flagged"  => Status.Flagged
    case "Done"     => Status.Done
  }

  def statusToString(x: Status): String = x match {
    case Status.New      => "New"
    case Status.Verified => "Verified"
    case Status.Reviewed => "Reviewed"
    case Status.Curated  => "Curated"
    case Status.Flagged  => "Flagged"
    case Status.Done     => "Done"
  }
}
