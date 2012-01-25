package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import java.sql.Timestamp

class File(val url: String,
           val name: String,
           val file: Array[Byte],
           val creationTime: Timestamp,
           val deletionTime: Timestamp,
           val password: Option[String],
           val question: Option[String],
           val answer: Option[String]
         ) {
  def this() = this("url",
                    "name",
                    Array(),
                    new Timestamp(System.currentTimeMillis),
                    new Timestamp(System.currentTimeMillis),
                    Some("password"),
                    Some("question"),
                    Some("answer")
                  )
}

object Files extends Schema {
  val files = table[File]("FILES")
  on(files) { f => declare(
    f.url is(unique, indexed, dbType("varchar(255)")),
    f.name is (dbType("varchar(255)")),
    f.question is(dbType("varchar(255)")),
    f.password is(dbType("varchar(32)")),
    f.answer is(dbType("varchar(32)"))
  )}
}

