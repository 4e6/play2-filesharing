package lib

import scalaz._
import Scalaz._

import play.api.mvc._
import play.api.libs.Files.TemporaryFile

import akka.util.Duration
import akka.util.duration._
import java.sql.Timestamp

object Helpers {
  class NumericPimp[A: Numeric](val n: A) {
    import Numeric._
    private[this] lazy val num = implicitly[Numeric[A]]
    private[this] lazy val kibi = num.fromInt(1024)

    /* Returns bytes */
    def kB = num.times(n, kibi)
    def MB = num.times(kB, kibi)
    def GB = num.times(MB, kibi)
  }

  implicit def numPimp[A: Numeric](time: A) = new NumericPimp(time)

  implicit def longToTimestamp(l: Long) = new Timestamp(l)

  implicit def longToDuration(l: Long) = l.millis

  def timeNow = System.currentTimeMillis

  val bytePrefixes = Seq("B", "kB", "MB", "GB")

  def hash(salt: Long)(password: String): Array[Byte] = {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    digest.reset()
    digest.update(salt.toString.getBytes)
    digest.digest(password.getBytes("UTF-8"))
  }

  def verifyData(file: models.Record, key: String, data: String) = {
    file.getSecret(key) getOrElse Array.empty sameElements hash(file.creationTime.getTime)(data)
  }

  def getParam[T](key: String)(implicit request: Request[T]) = {
    val body = request.body match {
      case body: MultipartFormData[_] => body.asFormUrlEncoded
      case body: AnyContent => body.asFormUrlEncoded | Map.empty
      case body: Map[String, Seq[String]] => body
    }
    body.get(key).flatMap(_.headOption).toSuccess(key).liftFailNel
  }

  def getSomeFile(url: String) = {
    import org.squeryl.PrimitiveTypeMode._
    transaction(models.Storage.records lookup url)
  }

  def getFile(url: String) = {
    import org.squeryl.PrimitiveTypeMode._
    transaction(models.Storage.records lookup url)
      .toSuccess("file " + url + " not found").liftFailNel
  }
}
