package controllers

import play.api.mvc._

import lib.Helpers._

object Application extends Controller with ScalateEngine
  with Upload with Download {
  def index = Action {
    val params = Map(
      "menu" -> Set("Home"),
      "filesizeLimit" -> lib.Config.filesizeLimit.toString,
      "readableFilesize" -> readableSize(lib.Config.filesizeLimit))
    Ok(render("views/index.jade", params))
  }
}
