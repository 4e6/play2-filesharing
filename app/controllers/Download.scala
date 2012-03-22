package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api._
import play.api.mvc._

import Helpers._

trait Download {
  self: Controller with ScalateEngine =>

  def downloadIndex(url: String) = Action {
    def failure = Ok {
      render("views/fileNotFound.jade", "filename" -> url)
    }

    def success(f: models.File) = Ok {
      render(
        "views/downloadIndex.jade",
        "url" -> url,
        "filename" -> f.name,
        "password" -> f.password.isDefined,
        "question" -> f.question.isDefined)
    }

    getSomeFile(url).fold(success, failure)
  }

  def retrieveFile(url: String, password: String) = {
    def success(f: models.File) = Action {
      Ok.sendFile(
        content = new java.io.File(f.path),
        fileName = _ => f.name)
    }
    def failure = Action {
      Ok { render("views/fileNotFound.jade", "filename" -> url) }
    }

    val file = getSomeFile(url) filter { isRightPassword(_, password) }

    file.cata(success, failure)
  }

  def checkPassword = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    val file = urlParam("url").toOption flatMap getSomeFile
    val pass = urlParam("password").toOption

    val isCorrect = (file |@| pass) { isRightPassword }
    Ok(toJson(JsObject(Seq("correct" -> JsBoolean(isCorrect | false)))))
  }
}

