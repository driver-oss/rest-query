package xyz.driver.pdsuicommon.logging

final class PhiStringContext(val sc: StringContext) extends AnyVal {
  def phi(args: PhiString*): PhiString = {
    val phiArgs = args.map(_.text)
    new PhiString(sc.s(phiArgs: _*))
  }
}
