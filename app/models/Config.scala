package models

import scalaz._
import Scalaz._

object Config {
  val config = play.api.Play.current.configuration

  lazy val logStatements_? = config.getBoolean("db.dev.logStatements") | false
  lazy val storagePath = config.getString("storage.path") | "files"
}
