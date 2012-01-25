package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema

class File(val name: String) {
  def this() = this("")
}

object Files extends Schema {
  val files = table[File]("FILES")
}

