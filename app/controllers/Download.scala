package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api._
import play.api.mvc._

import models._
import lib.Helpers._

trait Download {
  self: Controller with ScalateEngine =>

  def downloadIndex(url: String) = Action {
    def failure(l: NonEmptyList[String]) = Ok {
      render("views/fileNotFound.jade", "filename" -> url)
    }

    def success(r: Record) = {
      val params = Map(
        "url" -> url,
        "filename" -> r.name,
        "filesize" -> readableSize(r.size),
        "deletionTime" -> readableTime(r.timeLeft),
        "hasPassword" -> r.password.isDefined,
        "question" -> r.question.getOrElse(""))

      Ok { render("views/downloadIndex.jade", params) }
    }

    Record.get(url).fold(failure, success)
  }

  def GetAction[A](bp: BodyParser[A] = parse.anyContent): Action[A] =
    Action(bp) { implicit request =>
      def success(r: Record) = {
        val content = r.file
        SimpleResult(
          header = ResponseHeader(OK, Map(
            CONTENT_LENGTH -> content.length.toString,
            CONTENT_TYPE -> play.api.libs.MimeTypes.forFileName(content.getName)
              .getOrElse(play.api.http.ContentTypes.BINARY),
            CONTENT_DISPOSITION -> "attachment;"
          )),
          play.api.libs.iteratee.Enumerator.fromFile(content)
        )
      }

      def failure(l: NonEmptyList[String]) = Ok("Fail" + l.list)

      Logger.debug("apiGet body[" + request.body + "]")

      val record = Record.get
      val pass = Record.Password.get
      val answer = Record.Answer.get

      Record.verify(record, pass, answer).fold(failure, success)
    }

  def apiGetMultipart = GetAction(parse.multipartFormData)

  def apiGet(file: String) = GetAction()

  /** Check password or answer*/
  def checkSecret = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    val record = Record.get
    val pass = Record.Password.get
    val answer = Record.Answer.get

    val isCorrect = Record.verify(record, pass, answer).isSuccess

    Ok(toJson(JsObject(Seq("correct" -> JsBoolean(isCorrect)))))
  }
}
