package controllers

import scalaz._
import Scalaz._

import play.api._
import play.api.mvc._

import Helpers._
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

    getSomeFile(url).fold(success, failure)
  }

  def dlSendFile(url: String) = Action {
    def failure = Ok {
      render("views/downFailure.jade", "filename" -> url)
    }

    def success(f: File) = Ok(f.file)

    getSomeFile(url).fold(success, failure)
  }
}
