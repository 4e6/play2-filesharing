package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object Storage extends Schema {

  val root = scalax.file.Path.fromString(lib.Config.storagePath)

  val records = table[Record]("records")
  val schedule = table[Task]("schedule")

  on(records) { r =>
    declare(
      r.url is (unique, indexed, dbType("varchar(255)")),
      r.name is (dbType("varchar(255)")),
      r.question is (dbType("varchar(255)")))
  }

  on(schedule) { s =>
    declare(
      s.url is (unique, indexed, dbType("varchar(255)")))
  }

  def search(query: String, offset: Int, size: Int) = {
    def %%(query: String) = "%" + query + "%"
    val queryParts: Seq[String] =
      query.split(" ").filterNot(_ == query).distinct.map(%%)(collection.breakOut)

    transaction {
      val recordsEq = from(records)(r =>
        where(r.url === query or r.name === query)
          select (r)
          orderBy (r.creationTime asc)
      ).distinct

      val recordsLike = from(records)(r =>
        where((r.url like %%(query)) or (r.name like %%(query)))
          select (r)
          orderBy (r.creationTime asc)
      ).distinct

      val recordsResemble = queryParts map { part =>
        from(records)(r =>
          where((r.url like part) or (r.name like part))
            select (r)
        ).distinct
      }

      val results = (recordsEq ++ recordsLike ++ recordsResemble.flatten)(collection.breakOut).distinct

      results.size -> results.slice(offset, size)
    }
  }
}
