package lib

import scalaz._
import Scalaz._

import akka.util.duration._

object Config {
  val config = play.api.Play.current.configuration

  lazy val logStatements_? = config.getBoolean("db.dev.logStatements") | false
  lazy val storagePath = config.getString("storage.path") | "files"
  lazy val storageTime = 15.minutes.toMillis
}
