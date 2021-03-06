package lib

import scalaz._
import Scalaz._

import akka.util.duration._
import Helpers._

object Config {
  val config = play.api.Play.current.configuration

  lazy val appUrl = config.getString("app.url") | "http://localhost"
  lazy val logStatements_? = config.getBoolean("db.default.logStatements") | false
  lazy val storagePath = config.getString("storage.path") | "files"
  lazy val storageTime = config.getMilliseconds("storage.time") | 15.minutes.toMillis
  lazy val filesizeLimit = config.getBytes("storage.filesizeLimit") | 5.M
  lazy val resultsPerPage = config.getInt("search.resultsPerPage") | 10
  lazy val maxInputSize = 255
}
