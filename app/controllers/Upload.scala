package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api._
import play.api.mvc._
import MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import akka.util.duration._

import models._
import lib.Helpers._

trait Upload {
  self: Controller =>

  def Result[T](
    failure: NonEmptyList[String] => SimpleResult[T],
    success: Record => SimpleResult[T])(
      implicit r: Request[MultipartFormData[TemporaryFile]]) = {
    lazy val now = timeNow

    val file = Record.File.apply
    val url = Record.URL(file)
    val password = Record.Password(now)
    val question = Record.Question.apply
    val answer = Record.Answer(now)

    val result = Record(file, url, password, question, answer, now)

    result.fold(failure, success)
  }

  def failure(l: NonEmptyList[String]) = Ok("Failure" + l.list)

  def success(record: Record) = {
    import org.squeryl.PrimitiveTypeMode._
    val task = new Task(record.url, record.deletionTime)
    transaction {
      Storage.records insert record
      Storage.schedule insert task
    }
    Ok("Success(" + record.name + ")")
  }

  def apiUpload = Action(parse.multipartFormData) { implicit request =>
    Logger.debug("apiUpload body[" + request.body + "]")

    Result(failure, success)
  }

  def uploadFile = Action(parse.multipartFormData) { implicit request =>
    Logger.debug("uploadFile body[" + request.body + "]")

    Result(failure, success)
  }

  /** Check url availability*/
  def checkUrl = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    def failure(l: NonEmptyList[String]) = l.list mkString ", "

    def success(f: String) = "available"

    Logger.debug("checkUrl request body[" + request.body + "]")

    val url = Record.URL.get

    val msg = url.fold(failure, success)

    Ok(toJson(JsObject(Seq("available" -> JsBoolean(url.isSuccess), "msg" -> JsString(msg)))))
  }
}
