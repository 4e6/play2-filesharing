package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api.Logger
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

  def search(q: String, p: Int) = Action { implicit request =>
    import lib.Config.resultsPerPage

    val menu: Map[String, Any] = Map("menu" -> Set("Search"))

    def failure: Result = Ok(render("views/search.jade", menu))

    def success(query: String): Result = {
      val (size, results) = models.Storage.search(query, (p - 1) * resultsPerPage, resultsPerPage + 1)

      val params = menu ++ Map(
        "records" -> results,
        "page" -> p,
        "totalPages" -> (size / resultsPerPage + 1),
        "query" -> query,
        "totalResults" -> size)

      Ok(render("views/search.jade", params))
    }

    (q.isEmpty) ? failure | success(q)
  }
}
