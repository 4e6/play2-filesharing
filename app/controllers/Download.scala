package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api._
import play.api.mvc._

import Helpers._

trait Download {
  self: Controller with ScalateEngine =>

  def dlIndex(url: String) = Action {
    def failure = Ok {
      render("views/downFailure.jade", "filename" -> url)
    }

    def success(f: models.File) = Ok {
      render("views/downSuccess.jade",
        "url" -> url,
        "filename" -> f.name,
        "password" -> f.password.isDefined,
        "question" -> f.question.isDefined)
    }

    getSomeFile(url).fold(success, failure)
  }

  def dlSendFile(url: String) = Action {
    def failure = Ok {
      render("views/downFailure.jade", "filename" -> url)
    }

    def success(f: models.File) = Ok(f.file)

    getSomeFile(url).fold(success, failure)
  }

  def checkPassword = Action(parse.urlFormEncoded) { implicit request =>
    import play.api.libs.json._
    import Json._

    val file = urlParam("url").toOption flatMap getSomeFile
    val pass = urlParam("password").toOption
    val correct_? = (file |@| pass) { (f, p) =>
      hash(f.creationTime.getTime)(p) sameElements f.password.getOrElse(Array.empty)
    }

    Ok(toJson(JsObject(Seq("correct" -> JsBoolean(correct_? | false)))))
  }
}
