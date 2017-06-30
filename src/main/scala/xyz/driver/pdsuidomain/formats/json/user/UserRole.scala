package xyz.driver.pdsuidomain.formats.json.user

import xyz.driver.pdsuicommon.domain.User.Role

object UserRole {

  val roleFromString: PartialFunction[String, Role] = {
    case "Cleaner"                => Role.RecordCleaner
    case "Organizer"              => Role.RecordOrganizer
    case "Extractor"              => Role.DocumentExtractor
    case "RecordAdmin"            => Role.RecordAdmin
    case "TrialSummarizer"        => Role.TrialSummarizer
    case "CriteriaCurator"        => Role.CriteriaCurator
    case "TrialAdmin"             => Role.TrialAdmin
    case "EligibilityVerifier"    => Role.EligibilityVerifier
    case "TreatmentMatchingAdmin" => Role.TreatmentMatchingAdmin
    case "RoutesCurator"          => Role.RoutesCurator
    case "SystemUser"             => Role.SystemUser
    case "ResearchOncologist"     => Role.ResearchOncologist
    // No Mixed at this time
  }

  def roleToString(x: Role): String = x match {
    case Role.RecordCleaner          => "Cleaner"
    case Role.RecordOrganizer        => "Organizer"
    case Role.DocumentExtractor      => "Extractor"
    case Role.RecordAdmin            => "RecordAdmin"
    case Role.TrialSummarizer        => "TrialSummarizer"
    case Role.CriteriaCurator        => "CriteriaCurator"
    case Role.TrialAdmin             => "TrialAdmin"
    case Role.EligibilityVerifier    => "EligibilityVerifier"
    case Role.TreatmentMatchingAdmin => "TreatmentMatchingAdmin"
    case Role.RoutesCurator          => "RoutesCurator"
    case Role.SystemUser             => "SystemUser"
    case Role.ResearchOncologist     => "ResearchOncologist"
  }
}
