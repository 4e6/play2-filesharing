package models

import org.squeryl.{ Schema, KeyedEntity }
import org.squeryl.PrimitiveTypeMode._
import java.sql.Timestamp

class File(val url: String,
           val name: String,
           val file: Array[Byte],
           val creationTime: Timestamp,
           val deletionTime: Timestamp,
           val password: Option[Array[Byte]],
           val question: Option[String],
           val answer: Option[Array[Byte]])
    extends KeyedEntity[String] {
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

      def id = url
}

object Files extends Schema {
  val fileSize = 25 * 1024 * 1024

  val files = table[File]("FILES")

  on(files) { f =>
    declare(
      f.url is (unique, indexed, dbType("varchar(255)")),
      f.name is (dbType("varchar(255)")),
      f.file is (dbType("binary(" + fileSize + ")")),
      f.question is (dbType("varchar(255)")),
      f.password is (dbType("binary(32)")),
      f.answer is (dbType("binary(32)")))
  }
}
