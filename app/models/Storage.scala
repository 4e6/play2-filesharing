package models

import org.squeryl.Schema
import org.squeryl.PrimitiveTypeMode._

object Storage extends Schema {

  val root = scalax.file.Path(lib.Config.storagePath)

  val records = table[Record]("FILES")
  val schedule = table[Task]("SCHEDULE")

  on(records) { f =>
    declare(
      f.url is (unique, indexed, dbType("varchar(255)")),
      f.name is (dbType("varchar(255)")),
      f.question is (dbType("varchar(255)")),
      f.password is (dbType("binary(32)")),
      f.answer is (dbType("binary(32)")))
  }
}
