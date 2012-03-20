package controllers

import scalaz._
import Scalaz._

import play.api._
import play.api.mvc._

object Helpers {
  class NumericPimp[A: Numeric](val n: A) {
    import Numeric._
    private[this] lazy val num = implicitly[Numeric[A]]
    private[this] lazy val kibi = num.fromInt(1024)

    /* Returns milliseconds */
    def sec = num.times(n, num.fromInt(1000))
    def min = num.times(sec, num.fromInt(60))
    def hour = num.times(min, num.fromInt(60))
    def hours = hour
    def day = num.times(hour, num.fromInt(24))
    def days = day

    /* Returns bytes */
    def kB = num.times(n, kibi)
    def MB = num.times(kB, kibi)
    def GB = num.times(MB, kibi)

    def timestamp = new java.sql.Timestamp(num.toLong(n))
  }

  def hash(salt: Long)(password: String): Array[Byte] = {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
    digest.reset()
    digest.update(salt.toString.getBytes)
    digest.digest(password.getBytes("UTF-8"))
  }

  def isRightPassword(file: models.File, pass: String) =
    hash(file.creationTime.getTime)(pass) sameElements file.password.getOrElse(Array.empty)

  def multipartParam(key: String)(implicit request: Request[MultipartFormData[_]]) =
    request.body.dataParts.get(key).flatMap(_.headOption).toSuccess(key).liftFailNel

  def urlParam(key: String)(implicit request: Request[Map[String, Seq[String]]]) =
    request.body.get(key).flatMap(_.headOption).toSuccess(key).liftFailNel

  def getSomeFile(url: String) = {
    import org.squeryl.PrimitiveTypeMode._
    transaction(models.Files.files lookup url)
  }

  implicit def numPimp[A: Numeric](time: A) = new NumericPimp(time)
}
