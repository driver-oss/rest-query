package xyz.driver.pdsuicommon.db

import xyz.driver.pdsuicommon.logging._

import scala.collection.generic.CanBuildFrom

sealed trait SortingOrder
object SortingOrder {

  case object Ascending  extends SortingOrder
  case object Descending extends SortingOrder

}

sealed trait Sorting

object Sorting {

  val Empty = Sequential(Seq.empty)

  /**
    * @param tableName None if the table is default (same)
    * @param name      Dimension name
    * @param order     Order
    */
  case class Dimension(tableName: Option[String], name: String, order: SortingOrder) extends Sorting {
    def isForeign: Boolean = tableName.isDefined
  }

  case class Sequential(sorting: Seq[Dimension]) extends Sorting {
    override def toString: String = if (isEmpty(this)) "Empty" else super.toString
  }

  def isEmpty(input: Sorting): Boolean = {
    input match {
      case Sequential(Seq()) => true
      case _                 => false
    }
  }

  def filter(sorting: Sorting, p: Dimension => Boolean): Seq[Dimension] = sorting match {
    case x: Dimension if p(x) => Seq(x)
    case x: Dimension         => Seq.empty
    case Sequential(xs)       => xs.filter(p)
  }

  def collect[B, That](sorting: Sorting)(f: PartialFunction[Dimension, B])(
          implicit bf: CanBuildFrom[Seq[Dimension], B, That]): That = sorting match {
    case x: Dimension if f.isDefinedAt(x) =>
      val r = bf.apply()
      r += f(x)
      r.result()

    case x: Dimension   => bf.apply().result()
    case Sequential(xs) => xs.collect(f)
  }

  // Contains dimensions and ordering only, thus it is safe.
  implicit def toPhiString(x: Sorting): PhiString = Unsafe(x.toString)

}
