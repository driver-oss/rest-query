package xyz.driver.pdsuicommon.synchronization.db

import slick.dbio.DBIO

import scalaz.OptionT

trait SlickDataSource[T] {

  val isDictionary: Boolean = false

  /**
    * @return New entity
    */
  def create(x: T): DBIO[T]

  /**
    * @return Updated entity
    */
  def update(x: T): OptionT[DBIO, T]

  def delete(x: T): OptionT[DBIO, Unit]

}
