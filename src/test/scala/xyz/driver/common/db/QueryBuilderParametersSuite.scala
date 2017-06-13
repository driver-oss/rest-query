package xyz.driver.common.db

import java.time.LocalDateTime

import io.getquill.MysqlEscape
import org.scalatest.FreeSpecLike
import xyz.driver.common.db.QueryBuilder.TableData
import xyz.driver.common.domain.{Email, LongId, User}

class QueryBuilderParametersSuite extends FreeSpecLike {

  import SearchFilterBinaryOperation._
  import SearchFilterExpr.{Dimension => _, _}
  import SearchFilterNAryOperation._
  import Sorting._
  import SortingOrder._

  val tableName = "Entity"

  case class Entity(id: LongId[Entity],
                    name: String,
                    email: Email,
                    optionUser: Option[LongId[User]],
                    date: LocalDateTime,
                    optionDate: Option[LocalDateTime],
                    kindId: Long)

  def queryBuilderParameters = MysqlQueryBuilderParameters(
    tableData = TableData(
      tableName = tableName,
      nullableFields = Set("optionUser", "optionDate")
    ),
    links = Map(
      "Kind" -> TableLink("kindId", "Kind", "id"),
      "User" -> TableLink("optionUser", "User", "id")
    )
  )

  val queryBasis =
    s"""select `$tableName`.*
        |from `$tableName`""".stripMargin.trim

  "toSql" - {
    "should generate correct SQL query" - {
      "with default parameters" in {
        val (sql, _) = queryBuilderParameters.toSql(namingStrategy = MysqlEscape)
        assert(sql == queryBasis)
      }

      "with filtering: " - {
        "single atom filter" in {
          val (sql, _) = queryBuilderParameters.copy(filter = Atom.Binary("name", Eq, "x")).toSql(namingStrategy = MysqlEscape)
          assert(sql ==
            s"""$queryBasis
               |where `$tableName`.`name` = ?""".stripMargin)
        }

        "single atom filter for optional field with NotEq operation" in {
          val (sql, _) = queryBuilderParameters.copy(filter = Atom.Binary("optionUser", NotEq, "x")).toSql(namingStrategy = MysqlEscape)
          assert(sql ==
            s"""$queryBasis
               |where (`$tableName`.`optionUser` is null or `$tableName`.`optionUser` != ?)""".stripMargin)
        }

        "single atom filter for field with IN operation" in {
          val (sql, _) = queryBuilderParameters.copy(filter = Atom.NAry("date", In, Seq("x", "x", "x"))).toSql(namingStrategy = MysqlEscape)
          assert(sql ==
            s"""$queryBasis
               |where `$tableName`.`date` in (?, ?, ?)""".stripMargin)
        }

        "multiple intersected filters" in {
          val (sql, _) = queryBuilderParameters.copy(filter = Intersection(Seq(
            Atom.Binary("name", Gt, "x"),
            Atom.Binary("optionDate", GtEq, "x")
          ))).toSql(namingStrategy = MysqlEscape)
          assert(sql ==
            s"""$queryBasis
               |where (`$tableName`.`name` > ? and `$tableName`.`optionDate` >= ?)""".stripMargin)
        }

        "multiple intersected nested filters" in {
          val (sql, _) = queryBuilderParameters.copy(filter = Intersection(Seq(
            Atom.Binary("name", Gt, "x"),
            Atom.Binary("optionDate", GtEq, "x"),
            Intersection(Seq(
              Atom.Binary("optionUser", Eq, "x"),
              Atom.Binary("date", LtEq, "x")
            ))
          ))).toSql(namingStrategy = MysqlEscape)
          assert(sql ==
            s"$queryBasis\nwhere (`$tableName`.`name` > ? and `$tableName`.`optionDate` >= ?" +
              s" and (`$tableName`.`optionUser` = ? and `$tableName`.`date` <= ?))")
        }

        "multiple unionized filters" in {
          val (sql, _) = queryBuilderParameters.copy(filter = Union(Seq(
            Atom.Binary("name", Gt, "x"),
            Atom.Binary("optionDate", GtEq, "x")
          ))).toSql(namingStrategy = MysqlEscape)
          assert(sql ==
            s"""$queryBasis
               |where (`$tableName`.`name` > ? or `$tableName`.`optionDate` >= ?)""".stripMargin.trim)
        }

        "multiple unionized nested filters" in {
          val (sql, _) = queryBuilderParameters.copy(filter = Union(Seq(
            Atom.Binary("name", Gt, "x"),
            Atom.Binary("optionDate", GtEq, "x"),
            Union(Seq(
              Atom.Binary("optionUser", Eq, "x"),
              Atom.Binary("date", LtEq, "x")
            ))
          ))).toSql(namingStrategy = MysqlEscape)
          assert(sql ==
            s"""$queryBasis
               |where (`$tableName`.`name` > ? or `$tableName`.`optionDate` >= ? or (`$tableName`.`optionUser` = ? or `$tableName`.`date` <= ?))""".stripMargin)
        }

        "multiple unionized and intersected nested filters" in {
          val (sql, _) = queryBuilderParameters.copy(filter = Union(Seq(
            Intersection(Seq(
              Atom.Binary("name", Gt, "x"),
              Atom.Binary("optionDate", GtEq, "x")
            )),
            Intersection(Seq(
              Atom.Binary("optionUser", Eq, "x"),
              Atom.Binary("date", LtEq, "x")
            ))
          ))).toSql(namingStrategy = MysqlEscape)

          assert(sql ==
            s"$queryBasis\nwhere ((`$tableName`.`name` > ? and `$tableName`.`optionDate` >= ?) " +
              s"or (`$tableName`.`optionUser` = ? and `$tableName`.`date` <= ?))")
        }

        "single field from foreign table" in {
          val (sql, _) = queryBuilderParameters
            .copy(filter = Atom.Binary(SearchFilterExpr.Dimension(Some("Kind"), "name"), Eq, "x"))
            .toSql(namingStrategy = MysqlEscape)
          val pattern =
            s"""select `$tableName`.*
               |from `$tableName`
               |inner join `Kind` on `Entity`.`kindId` = `Kind`.`id`
               |where `Kind`.`name` = ?""".stripMargin
          assert(sql == pattern)
        }
      }

      "with sorting:" - {
        "single field sorting" in {
          val (sql, _) = queryBuilderParameters.copy(sorting = Dimension(None, "name", Ascending)).toSql(namingStrategy = MysqlEscape)

          assert(sql ==
            s"""$queryBasis
               |order by `$tableName`.`name` asc""".stripMargin)
        }

        "single foreign sorting field" in {
          val (sql, _) = queryBuilderParameters.copy(sorting = Dimension(Some("Kind"), "name", Ascending)).toSql(namingStrategy = MysqlEscape)

          assert(sql ==
            s"""select `$tableName`.*
                |from `$tableName`
                |inner join `Kind` on `Entity`.`kindId` = `Kind`.`id`
                |order by `Kind`.`name` asc""".stripMargin)
        }

        "multiple fields sorting" in {
          val (sql, _) = queryBuilderParameters.copy(sorting = Sequential(Seq(
            Dimension(None, "name", Ascending),
            Dimension(None, "date", Descending)
          ))).toSql(namingStrategy = MysqlEscape)
          assert(sql ==
            s"""$queryBasis
               |order by `$tableName`.`name` asc, `$tableName`.`date` desc""".stripMargin)
        }

        "multiple foreign sorting field" in {
          val (sql, _) = queryBuilderParameters.copy(sorting = Sequential(Seq(
            Dimension(Some("Kind"), "name", Ascending),
            Dimension(Some("User"), "name", Descending)
          ))).toSql(namingStrategy = MysqlEscape)

          assert(sql ==
            s"""select `$tableName`.*
                |from `$tableName`
                |inner join `Kind` on `$tableName`.`kindId` = `Kind`.`id`
                |inner join `User` on `$tableName`.`optionUser` = `User`.`id`
                |order by `Kind`.`name` asc, `User`.`name` desc""".stripMargin)
        }

        "multiple field sorting (including foreign tables)" in {
          val (sql, _) = queryBuilderParameters.copy(sorting = Sequential(Seq(
            Dimension(Some("Kind"), "name", Ascending),
            Dimension(None, "date", Descending)
          ))).toSql(namingStrategy = MysqlEscape)

          assert(sql ==
            s"""select `$tableName`.*
                |from `$tableName`
                |inner join `Kind` on `$tableName`.`kindId` = `Kind`.`id`
                |order by `Kind`.`name` asc, `$tableName`.`date` desc""".stripMargin)
        }
      }

      "with pagination" in {
        val (sql, _) = queryBuilderParameters.copy(pagination = Some(Pagination(5, 3))).toSql(namingStrategy = MysqlEscape)
        assert(sql ==
          s"""$queryBasis
             |limit 10, 5""".stripMargin)
      }

      "combined" in {
        val filter = Union(Seq(
          Intersection(Seq(
            Atom.Binary("name", Gt, "x"),
            Atom.Binary("optionDate", GtEq, "x")
          )),
          Intersection(Seq(
            Atom.Binary("optionUser", Eq, "x"),
            Atom.Binary("date", LtEq, "x")
          ))
        ))
        val sorting = Sequential(Seq(
          Dimension(Some("Kind"), "name", Ascending),
          Dimension(None, "name", Ascending),
          Dimension(None, "date", Descending)
        ))

        val (sql, _) = queryBuilderParameters.copy(
          filter = filter,
          sorting = sorting,
          pagination = Some(Pagination(5, 3))
        ).toSql(namingStrategy = MysqlEscape)

        assert(sql ==
          s"""select `$tableName`.*
              |from `$tableName`
              |inner join `Kind` on `$tableName`.`kindId` = `Kind`.`id`
              |where ((`$tableName`.`name` > ? and `$tableName`.`optionDate` >= ?) or (`$tableName`.`optionUser` = ? and `$tableName`.`date` <= ?))
              |order by `Kind`.`name` asc, `$tableName`.`name` asc, `$tableName`.`date` desc
              |limit 10, 5""".stripMargin)
      }

    }
  }

}
