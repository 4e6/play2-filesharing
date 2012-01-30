package controllers

import play.api._
import play.api.mvc._
import Helpers._

object Application extends Controller with ScalateEngine
    with Upload with Download {
  def index = Action {
    Ok(render("views/index.jade"))
  }
}
