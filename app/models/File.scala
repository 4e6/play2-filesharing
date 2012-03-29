package models

import org.squeryl.KeyedEntity
import java.sql.Timestamp
import controllers.Helpers._

class File(val url: String,
           val name: String,
           val size: Long,
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
    0 timestamp,
    0 timestamp,
    Some(Array.empty),
    Some("question"),
    Some(Array.empty))

  def id = url

  def path = Storage.root / url / name

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
