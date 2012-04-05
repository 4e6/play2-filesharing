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
    def failure = Ok {
      render("views/fileNotFound.jade", "filename" -> url)
    }

    def success(f: Record) = {
      val params = Map(
        "url" -> url,
        "filename" -> f.name,
        "filesize" -> f.readableSize,
        "password" -> f.password.isDefined,
        "question" -> f.question.getOrElse(""))

      Ok { render("views/downloadIndex.jade", params) }
    }

    getSomeFile(url).fold(success, failure)
  }

  /** Send file to user*/
  def retrieveFile(url: String, key: String, data: String) = {
    def success(f: Record) = Action {
      import scalax.file.Path
      import scalax.file.defaultfs.DefaultPath
      Ok.sendFile(
        content = f.path.asInstanceOf[DefaultPath].jfile,
        fileName = _ => f.name)
    }
    def failure = Action {
      Ok { render("views/fileNotFound.jade", "filename" -> url) }
    }

    val file = getSomeFile(url) filter { verifyData(_, key, data) }

    file.cata(success, failure)
  }

  /** Check password or answer*/
  def checkSecret = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    val file = urlParam("url").toOption flatMap getSomeFile
    val key = urlParam("key").toOption
    val data = urlParam("data").toOption

    val isCorrect = (file |@| key |@| data) { verifyData }

    Ok(toJson(JsObject(Seq("correct" -> JsBoolean(isCorrect | false)))))
  }
}
