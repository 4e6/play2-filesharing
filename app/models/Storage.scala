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
}
