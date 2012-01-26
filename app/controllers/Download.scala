package controllers

import play.api._
import play.api.mvc._

trait Download {
  self: Controller =>

  def downloadFile(url: String) = Action {
    Ok("got [" + url + "]")
  }
}
