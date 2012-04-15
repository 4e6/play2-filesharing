package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api.Logger
import play.api.mvc._

import models._
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

  def search(p: Int) = Action { implicit request =>
    Logger.info("search body[" + request.queryString + "]")

    val menu: Map[String, Any] = Map("menu" -> Set("Search"))

    val recordsPerPage = 10

    def failure: NonEmptyList[String] => Result = _ =>
      Ok(render("views/search.jade", menu))
    def success: String => Result = { q =>
      val query = "%" + q + "%"
      val (size, results) = getRecords(query, (p - 1) * recordsPerPage, recordsPerPage)

      val params = menu ++ Map(
        "records" -> results,
        "page" -> p,
        "totalPages" -> (size / recordsPerPage + 1),
        "query" -> q,
        "totalResults" -> size)

      Ok(render("views/search.jade", params))
    }

    def getRecords(name: String, offset: Int, size: Int) = {
      import org.squeryl.PrimitiveTypeMode._
      transaction {
        val query = from(Storage.records)(r =>
          where(r.url like name)
            select (r)
            orderBy (r.creationTime asc))
        query.size -> query.page(offset, size).toList
      }
    }

    val q = getParam("q")

    q.fold(failure, success)
  }
}
