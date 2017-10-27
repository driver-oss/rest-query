package xyz.driver.pdsuicommon.db.repositories

import xyz.driver.pdsuicommon.logging._

trait RepositoryLogging extends PhiLogging {

  protected def logCreatedOne[T](x: T)(implicit toPhiString: T => PhiString): T = {
    logger.info(phi"An entity was created: $x")
    x
  }

  protected def logCreatedMultiple[T <: Iterable[_]](xs: T)(implicit toPhiString: T => PhiString): T = {
    if (xs.nonEmpty) {
      logger.info(phi"Entities were created: $xs")
    }
    xs
  }

  protected def logUpdatedOne(rowsAffected: Long): Long = {
    rowsAffected match {
      case 0 => logger.trace(phi"The entity is up to date")
      case 1 => logger.info(phi"The entity was updated")
      case x => logger.warn(phi"The ${Unsafe(x)} entities were updated")
    }
    rowsAffected
  }

  protected def logUpdatedOneUnimportant(rowsAffected: Long): Long = {
    rowsAffected match {
      case 0 => logger.trace(phi"The entity is up to date")
      case 1 => logger.trace(phi"The entity was updated")
      case x => logger.warn(phi"The ${Unsafe(x)} entities were updated")
    }
    rowsAffected
  }

  protected def logUpdatedMultiple(rowsAffected: Long): Long = {
    rowsAffected match {
      case 0 => logger.trace(phi"All entities are up to date")
      case x => logger.info(phi"The ${Unsafe(x)} entities were updated")
    }
    rowsAffected
  }

  protected def logDeletedOne(rowsAffected: Long): Long = {
    rowsAffected match {
      case 0 => logger.trace(phi"The entity does not exist")
      case 1 => logger.info(phi"The entity was deleted")
      case x => logger.warn(phi"Deleted ${Unsafe(x)} entities, expected one")
    }
    rowsAffected
  }

  protected def logDeletedMultiple(rowsAffected: Long): Long = {
    rowsAffected match {
      case 0 => logger.trace(phi"Entities do not exist")
      case x => logger.info(phi"Deleted ${Unsafe(x)} entities")
    }
    rowsAffected
  }

}
