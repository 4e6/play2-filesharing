package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import java.sql.Timestamp

class File(val url: String,
           val name: String,
           val file: Array[Byte],
           val creationTime: Timestamp,
           val deletionTime: Timestamp,
           val password: Option[Array[Byte]],
           val question: Option[String],
           val answer: Option[Array[Byte]]) {
  def this() =
    this(
      "url",
      "name",
      Array(),
      new Timestamp(System.currentTimeMillis),
      new Timestamp(System.currentTimeMillis),
      Some(Array()),
      Some("question"),
      Some(Array())
    )
}

object Files extends Schema {
  val bytesPerFile = 25 * 1024 * 1024

  val files = table[File]("FILES")

  on(files) { f =>
    declare(
      f.url is (unique, indexed, dbType("varchar(255)")),
      f.name is (dbType("varchar(255)")),
      f.file is (dbType("binary(" + bytesPerFile + ")")),
      f.question is (dbType("varchar(255)")),
      f.password is (dbType("binary(32)")),
      f.answer is (dbType("binary(32)")))
  }
}
