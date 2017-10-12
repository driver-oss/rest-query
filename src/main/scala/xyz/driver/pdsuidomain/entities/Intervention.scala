package xyz.driver.pdsuidomain.entities

import xyz.driver.pdsuicommon.domain.{LongId, StringId}
import xyz.driver.pdsuicommon.logging._
import xyz.driver.pdsuidomain.entities.InterventionType.DeliveryMethod
import xyz.driver.pdsuidomain.entities.InterventionType.DeliveryMethod._

sealed trait InterventionType {
  val id: LongId[InterventionType]
  val name: String
  val deliveryMethods: Set[DeliveryMethod]
}

object InterventionType {

  final case object RadiationTherapy extends InterventionType {
    val id: LongId[InterventionType]         = LongId[InterventionType](1)
    val name: String                         = "Radiation therapy"
    val deliveryMethods: Set[DeliveryMethod] = commonMethods
  }

  final case object Chemotherapy extends InterventionType {
    val id: LongId[InterventionType]         = LongId[InterventionType](2)
    val name: String                         = "Chemotherapy"
    val deliveryMethods: Set[DeliveryMethod] = commonMethods
  }

  final case object TargetedTherapy extends InterventionType {
    val id: LongId[InterventionType]         = LongId[InterventionType](3)
    val name: String                         = "Targeted therapy"
    val deliveryMethods: Set[DeliveryMethod] = commonMethods
  }

  final case object Immunotherapy extends InterventionType {
    val id: LongId[InterventionType]         = LongId[InterventionType](4)
    val name: String                         = "Immunotherapy"
    val deliveryMethods: Set[DeliveryMethod] = commonMethods
  }

  final case object Surgery extends InterventionType {
    val id: LongId[InterventionType]         = LongId[InterventionType](5)
    val name: String                         = "Surgery"
    val deliveryMethods: Set[DeliveryMethod] = commonMethods
  }

  final case object HormoneTherapy extends InterventionType {
    val id: LongId[InterventionType]         = LongId[InterventionType](6)
    val name: String                         = "Hormone therapy"
    val deliveryMethods: Set[DeliveryMethod] = commonMethods
  }

  final case object Other extends InterventionType {
    val id: LongId[InterventionType]         = LongId[InterventionType](7)
    val name: String                         = "Other"
    val deliveryMethods: Set[DeliveryMethod] = commonMethods
  }

  final case object Radiation extends InterventionType {
    val id: LongId[InterventionType] = LongId[InterventionType](8)
    val name: String                 = "Radiation"
    val deliveryMethods: Set[DeliveryMethod] = Set(
      ExternalRadiationTherapy,
      Brachytherapy,
      SystemicRadiationTherapyIV,
      SystemicRadiationTherapyOral,
      ProtonBeamTherapy
    )
  }

  final case object SurgeryProcedure extends InterventionType {
    val id: LongId[InterventionType] = LongId[InterventionType](9)
    val name: String                 = "Surgery/Procedure"
    val deliveryMethods: Set[DeliveryMethod] = Set(
      RadioFrequencyAblationRFA,
      Cryoablation,
      TherapeuticConventionalSurgery,
      RoboticAssistedLaparoscopicSurgery
    )
  }

  def typeFromString: PartialFunction[String, InterventionType] = {
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

  val All: Map[LongId[InterventionType], InterventionType] = Map[LongId[InterventionType], InterventionType](
    LongId[InterventionType](1) -> RadiationTherapy,
    LongId[InterventionType](2) -> Chemotherapy,
    LongId[InterventionType](3) -> TargetedTherapy,
    LongId[InterventionType](4) -> Immunotherapy,
    LongId[InterventionType](5) -> Surgery,
    LongId[InterventionType](6) -> HormoneTherapy,
    LongId[InterventionType](7) -> Other,
    LongId[InterventionType](8) -> Radiation,
    LongId[InterventionType](9) -> SurgeryProcedure
  )

  implicit def toPhiString(x: InterventionType): PhiString = {
    import x._
    phi"InterventionType(id=$id, name=${Unsafe(name)})"
  }
}

final case class InterventionArm(armId: LongId[SlotArm], interventionId: LongId[Intervention])

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
                              deliveryMethod: Option[String]) {
  def deliveryMethodIsCorrect: Boolean = {
    if (this.typeId.nonEmpty && this.deliveryMethod.nonEmpty) {
      InterventionType.All
        .getOrElse(this.typeId.get, throw new IllegalArgumentException(s"Not found Intervention type ${this.typeId}"))
        .deliveryMethods
        .contains(DeliveryMethod.fromString(this.deliveryMethod.get))
    } else true
  }
}

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
