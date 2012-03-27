package models

import org.squeryl.{ Schema, KeyedEntity }
import org.squeryl.PrimitiveTypeMode._
import java.sql.Timestamp
import controllers.Helpers._

class File(val url: String,
           val name: String,
           val size: Long,
           val path: String,
           val creationTime: Timestamp,
           val deletionTime: Timestamp,
           val password: Option[Array[Byte]],
           val question: Option[String],
           val answer: Option[Array[Byte]])
    extends KeyedEntity[String] {

  def this() = this(
    "url",
    "name",
    0,
    "path",
    0 timestamp,
    0 timestamp,
    Some(Array.empty),
    Some("question"),
    Some(Array.empty))

  def id = url

  def getSecret(key: String) = key match {
    case "password" => password
    case "answer" => answer
    case _ => None
  }

  def readableSize = {
    val mask = "%.1f"
    def convert(size: Double, px: Seq[String]): String = {
      val next = size / 1.kB
      if (px.nonEmpty && next > 1) convert(next, px.tail)
      else mask.format(size) + " " + px.head
    }

    convert(size, bytePrefixes)
  }
}

object Storage extends Schema {
  val files = table[File]("FILES")

  def path(url: String, name: String) =
    "/mnt/storage/webcb/files/" + url + "/" + name

  on(files) { f =>
    declare(
      f.url is (unique, indexed, dbType("varchar(255)")),
      f.name is (dbType("varchar(255)")),
      f.path is (dbType("varchar(255)")),
      f.question is (dbType("varchar(255)")),
      f.password is (dbType("binary(32)")),
      f.answer is (dbType("binary(32)")))
  }
}
