package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object Storage extends Schema {

  val root = scalax.file.Path.fromString(lib.Config.storagePath)

  val records = table[Record]("RECORDS")
  val schedule = table[Task]("SCHEDULE")

  on(records) { r =>
    declare(
      r.url is (unique, indexed, dbType("varchar(255)")),
      r.name is (dbType("varchar(255)")),
      r.question is (dbType("varchar(255)")),
      r.password is (dbType("binary(32)")),
      r.answer is (dbType("binary(32)")))
  }

  on(schedule) { s =>
    declare(
      s.url is (unique, indexed, dbType("varchar(255)")))
  }
}
