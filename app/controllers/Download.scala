package controllers

import scalaz._
import Scalaz._

import play.api._
import play.api.mvc._

import org.squeryl.PrimitiveTypeMode._
import models.File
import models.Files._

trait Download {
  self: Controller =>

  def downloadFile(url: String) = Action {
    transaction {
      val file = files.where(f => f.url === url).headOption
      file.cata(
        s => Ok("Got [" + s.url + "]"),
        Ok("File [" + url + "] not found"))
    }
  }
}
