package xyz.driver.pdsuicommon.synchronization.utils

/**
  * Allows to extract a data from the From entity to convert/update in to the To entity.
  */
trait Refiner[-From, To] {

  def refine(raw: From): To

  def refresh(orig: To, update: From): To

}
