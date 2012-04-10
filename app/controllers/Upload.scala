package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api._
import play.api.mvc._
import MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile

import models._
import lib.Helpers._

trait Upload {
  self: Controller with ScalateEngine =>

  type StringNEL = NonEmptyList[String]

  def Upload(onFailure: StringNEL => Result, onSuccess: Record => Result) =
    Action(parse.multipartFormData) { implicit request =>
      Logger.debug("Upload body[" + request.body + "]")

      def success(record: Record) = {
        import org.squeryl.PrimitiveTypeMode._
        val task = new Task(record.url, record.deletionTime)
        transaction {
          Storage.records insert record
          Storage.schedule insert task
        }
        onSuccess(record)
      }

      lazy val now = timeNow

      val file = Record.File.apply
      val url = Record.URL(file)
      val password = Record.Password(now)
      val question = Record.Question.apply
      val answer = Record.Answer(now)

      val result = Record(file, url, password, question, answer, now)

      result.fold(onFailure, success)
    }

  def apiUpload = {
    def failure(l: StringNEL) = Ok("Failure" + l.list)
    def success(record: Record) = Ok("Success(" + record.name + ")")

    Upload(failure, success)
  }

  def webUpload = {
    def failure(l: NonEmptyList[String]) = Ok("Failure" + l.list)
    def success(r: Record) = Ok {
      val params = Map(
        "url" -> r.url,
        "time" -> readableTime(r.timeLeft))
      render("views/uploadSuccess.jade", params)
    }
    Upload(failure, success)
  }

  /** Check url availability*/
  def checkUrl = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    def failure(l: NonEmptyList[String]) = l.list mkString ", "

    def success(f: String) = "available"

    Logger.debug("checkUrl request body[" + request.body + "]")

    val url = Record.URL.apply

    val msg = url.fold(failure, success)

    Ok(toJson(JsObject(Seq("available" -> JsBoolean(url.isSuccess), "msg" -> JsString(msg)))))
  }
}
