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
    def timeLeftMsg(ts: List[Long]) = ts match {
      case Nil => "Error"
      case 0 :: _ => "in a minute"
      case mins :: 0 :: _ => "%d minutes".format(mins)
      case mins :: hours :: 0 :: _ => "%d hours %d minutes".format(hours, mins)
      case mins :: hours :: days :: _ => "%d days".format(days)
    }

    def failure(l: NonEmptyList[String]) = Ok {
      render("views/fileNotFound.jade", "filename" -> url)
    }

    def success(r: Record) = {
      val params = Map(
        "url" -> url,
        "filename" -> r.name,
        "filesize" -> r.readableSize,
        "deletionTime" -> timeLeftMsg(r.timeLeft),
        "hasPassword" -> r.password.isDefined,
        "question" -> r.question.getOrElse(""))

      Ok { render("views/downloadIndex.jade", params) }
    }

    Record.get(url).fold(failure, success)
  }

  def GetAction[A](bp: BodyParser[A] = parse.anyContent): Action[A] =
    Action(bp) { implicit request =>
      def success(r: Record) = Ok.sendFile(
        content = r.file,
        fileName = _ => r.name)

      def failure(l: NonEmptyList[String]) = Ok("Fail" + l.list)

      Logger.debug("apiGet body[" + request.body + "]")

      val url = Record.URL.get
      val record = url flatMap { Record.get }
      val pass = Record.Password.get
      val answer = Record.Answer.get

      Record.verify(record, pass, answer).fold(failure, success)
    }

  def apiGetMultipart = GetAction(parse.multipartFormData)

  def apiGet = GetAction()

  /** Check password or answer*/
  def checkSecret = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    val file = getParam("url").toOption flatMap getSomeFile
    val key = getParam("key").toOption
    val data = getParam("data").toOption

    val isCorrect = (file |@| key |@| data) { verifyData }

    Ok(toJson(JsObject(Seq("correct" -> JsBoolean(isCorrect | false)))))
  }
}
