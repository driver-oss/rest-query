package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.InterventionType.DeliveryMethod._

final case class InterventionType(id: LongId[InterventionType], name: String)

object InterventionType {

  sealed trait InterventionTypeName
  case object RadiationTherapy extends InterventionTypeName
  case object Chemotherapy     extends InterventionTypeName
  case object TargetedTherapy  extends InterventionTypeName
  case object Immunotherapy    extends InterventionTypeName
  case object Surgery          extends InterventionTypeName
  case object HormoneTherapy   extends InterventionTypeName
  case object Other            extends InterventionTypeName
  case object Radiation        extends InterventionTypeName
  case object SurgeryProcedure extends InterventionTypeName

  def typeFromString: PartialFunction[String, InterventionTypeName] = {
    case "Radiation therapy" => RadiationTherapy
    case "Chemotherapy"      => Chemotherapy
    case "Targeted therapy"  => TargetedTherapy
    case "Immunotherapy"     => Immunotherapy
    case "Surgery"           => Surgery
    case "Hormone therapy"   => HormoneTherapy
    case "Other"             => Other
    case "Radiation"         => Radiation
    case "Surgery/Procedure" => SurgeryProcedure
  }

  def typeToString(x: InterventionTypeName): String = x match {
    case RadiationTherapy => "Radiation therapy"
    case Chemotherapy     => "Chemotherapy"
    case TargetedTherapy  => "Targeted therapy"
    case Immunotherapy    => "Immunotherapy"
    case Surgery          => "Surgery"
    case HormoneTherapy   => "Hormone therapy"
    case Other            => "Other"
    case Radiation        => "Radiation"
    case SurgeryProcedure => "Surgery/Procedure"
  }

  sealed trait DeliveryMethod
  object DeliveryMethod {
    case object IntravenousInfusionIV              extends DeliveryMethod
    case object IntramuscularInjection             extends DeliveryMethod
    case object SubcutaneousInjection              extends DeliveryMethod
    case object IntradermalInjection               extends DeliveryMethod
    case object SpinalInjection                    extends DeliveryMethod
    case object Oral                               extends DeliveryMethod
    case object Topical                            extends DeliveryMethod
    case object TransdermalPatch                   extends DeliveryMethod
    case object Inhalation                         extends DeliveryMethod
    case object Rectal                             extends DeliveryMethod
    case object ExternalRadiationTherapy           extends DeliveryMethod
    case object Brachytherapy                      extends DeliveryMethod
    case object SystemicRadiationTherapyIV         extends DeliveryMethod
    case object SystemicRadiationTherapyOral       extends DeliveryMethod
    case object ProtonBeamTherapy                  extends DeliveryMethod
    case object RadioFrequencyAblationRFA          extends DeliveryMethod
    case object Cryoablation                       extends DeliveryMethod
    case object TherapeuticConventionalSurgery     extends DeliveryMethod
    case object RoboticAssistedLaparoscopicSurgery extends DeliveryMethod

    def fromString: PartialFunction[String, DeliveryMethod] = {
      case "Intravenous Infusion (IV)"             => IntravenousInfusionIV
      case "Intramuscular Injection"               => IntramuscularInjection
      case "Subcutaneous Injection"                => SubcutaneousInjection
      case "Intradermal Injection"                 => IntradermalInjection
      case "Spinal Injection"                      => SpinalInjection
      case "Oral"                                  => Oral
      case "Topical"                               => Topical
      case "Transdermal Patch"                     => TransdermalPatch
      case "Inhalation"                            => Inhalation
      case "Rectal"                                => Rectal
      case "External Radiation Therapy"            => ExternalRadiationTherapy
      case "Brachytherapy"                         => Brachytherapy
      case "Systemic Radiation Therapy (IV)"       => SystemicRadiationTherapyIV
      case "Systemic Radiation Therapy (Oral)"     => SystemicRadiationTherapyOral
      case "Proton Beam Therapy"                   => ProtonBeamTherapy
      case "Radio-Frequency Ablation (RFA)"        => RadioFrequencyAblationRFA
      case "Cryoablation"                          => Cryoablation
      case "Therapeutic Conventional Surgery"      => TherapeuticConventionalSurgery
      case "Robotic Assisted Laparoscopic Surgery" => RoboticAssistedLaparoscopicSurgery
    }

    def methodToString(x: DeliveryMethod): String = x match {
      case IntravenousInfusionIV              => "Intravenous Infusion (IV)"
      case IntramuscularInjection             => "Intramuscular Injection"
      case SubcutaneousInjection              => "Subcutaneous Injection"
      case IntradermalInjection               => "Intradermal Injection"
      case SpinalInjection                    => "Spinal Injection"
      case Oral                               => "Oral"
      case Topical                            => "Topical"
      case TransdermalPatch                   => "Transdermal Patch"
      case Inhalation                         => "Inhalation"
      case Rectal                             => "Rectal"
      case ExternalRadiationTherapy           => "External Radiation Therapy"
      case Brachytherapy                      => "Brachytherapy"
      case SystemicRadiationTherapyIV         => "Systemic Radiation Therapy (IV)"
      case SystemicRadiationTherapyOral       => "Systemic Radiation Therapy (Oral)"
      case ProtonBeamTherapy                  => "Proton Beam Therapy"
      case RadioFrequencyAblationRFA          => "Radio-Frequency Ablation (RFA)"
      case Cryoablation                       => "Cryoablation"
      case TherapeuticConventionalSurgery     => "Therapeutic Conventional Surgery"
      case RoboticAssistedLaparoscopicSurgery => "Robotic Assisted Laparoscopic Surgery"
    }
  }

  val commonMethods = Set[DeliveryMethod](
    IntravenousInfusionIV,
    IntramuscularInjection,
    SubcutaneousInjection,
    IntradermalInjection,
    SpinalInjection,
    Oral,
    Topical,
    TransdermalPatch,
    Inhalation,
    Rectal
  )

  val deliveryMethodGroups: Map[LongId[InterventionType], Set[DeliveryMethod]] = Map(
    LongId(1) -> commonMethods,
    LongId(2) -> commonMethods,
    LongId(3) -> commonMethods,
    LongId(4) -> commonMethods,
    LongId(5) -> commonMethods,
    LongId(6) -> commonMethods,
    LongId(7) -> commonMethods,
    LongId(8) -> Set(
      ExternalRadiationTherapy,
      Brachytherapy,
      SystemicRadiationTherapyIV,
      SystemicRadiationTherapyOral,
      ProtonBeamTherapy
    ),
    LongId(9) -> Set(
      RadioFrequencyAblationRFA,
      Cryoablation,
      TherapeuticConventionalSurgery,
      RoboticAssistedLaparoscopicSurgery
    )
  )

  implicit def toPhiString(x: InterventionType): PhiString = {
    import x._
    phi"InterventionType(id=$id, name=${Unsafe(name)})"
  }
}

final case class InterventionArm(armId: LongId[Arm], interventionId: LongId[Intervention])

object InterventionArm {
  implicit def toPhiString(x: InterventionArm): PhiString = {
    import x._
    phi"InterventionArm(armId=$armId, interventionId=$interventionId)"
  }
}

final case class Intervention(id: LongId[Intervention],
                              trialId: StringId[Trial],
                              name: String,
                              originalName: String,
                              typeId: Option[LongId[InterventionType]],
                              originalType: Option[String],
                              dosage: String,
                              originalDosage: String,
                              isActive: Boolean,
                              deliveryMethod: Option[String])

object Intervention {
  implicit def toPhiString(x: Intervention): PhiString = {
    import x._
    phi"Intervention(id=$id, trialId=$trialId, name=${Unsafe(name)}, typeId=$typeId, isActive=$isActive, deliveryMethod=${Unsafe(deliveryMethod)})"
  }
}

final case class InterventionWithArms(intervention: Intervention, arms: List[InterventionArm])

object InterventionWithArms {
  implicit def toPhiString(x: InterventionWithArms): PhiString = {
    import x._
    phi"InterventionWithArms(intervention=$intervention, arms=$arms)"
  }
}
