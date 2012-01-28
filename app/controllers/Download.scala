package controllers

import scalaz._
import Scalaz._

import play.api._
import play.api.mvc._
import play.api.templates.HtmlFormat

import org.squeryl.PrimitiveTypeMode._
import models.File
import models.Files._

trait Download {
  self: Controller with ScalateEngine =>

  def dlIndex(url: String) = Action {
    def failure = Ok {
      render("views/downFailure.jade", "filename" -> url)
    }

    def success(f: File) = Ok {
      render("views/downSuccess.jade",
        "url" -> url,
        "filename" -> f.name,
        "password" -> f.password.isDefined,
        "question" -> f.question.isDefined)
    }

    transaction {
      val file = files.where(f => f.url === url).headOption //.toSuccess("file").liftFailNel
      file.fold(success, failure)
    }
  }

  def dlSendFile(url: String) = Action {
    def failure = Ok {
      render("views/downFailure.jade", "filename" -> url)
    }

    def success(f: File) = Ok(f.file)

    val file = transaction {
      files.where(_.url === url).headOption
    }
    file.fold(success, failure)
  }

}
