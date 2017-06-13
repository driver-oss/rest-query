package xyz.driver.common.utils

import xyz.driver.common.db.SearchFilterBinaryOperation.Eq
import xyz.driver.common.db.SearchFilterExpr
import xyz.driver.common.db.SearchFilterExpr.{Atom, Dimension}
import xyz.driver.common.logging._

import scala.util.{Failure, Success, Try}


object ServiceUtils extends PhiLogging {
  def findEqFilter(filter: SearchFilterExpr, fieldName: String): Option[SearchFilterExpr] = {
    findEqFilter(filter, Dimension(None, fieldName))
  }

  def findEqFilter(filter: SearchFilterExpr, dimension: Dimension): Option[SearchFilterExpr] = {
    filter.find {
      case Atom.Binary(dimension, Eq, _) => true
      case _ => false
    }
  }

  def convertIdInFilterToLong(value: AnyRef): Option[Long] = {
    Try(value.toString.toLong) match {
      case Success(id) =>
        Option(id)
      case Failure(e) =>
        logger.error(phi"Incorrect id format in filter $e")
        None
    }
  }
}
