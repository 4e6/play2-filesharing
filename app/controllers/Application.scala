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
      val (size, results) = getRecords(q, (p - 1) * recordsPerPage, recordsPerPage + 1)

      val params = menu ++ Map(
        "records" -> results,
        "page" -> p,
        "totalPages" -> (size / recordsPerPage + 1),
        "query" -> q,
        "totalResults" -> size)

      Ok(render("views/search.jade", params))
    }

    def getRecords(query: String, offset: Int, size: Int) = {
      import org.squeryl.PrimitiveTypeMode._
      transaction {
        val recordsEq = from(Storage.records)(r =>
          where(r.url === query)
            select (r)
            orderBy (r.creationTime asc))

        val recordsLike = from(Storage.records)(r =>
          where(r.url like ("%" + query + "%"))
            select (r)
            orderBy (r.creationTime asc))

        val recordsAll = (recordsEq ++ recordsLike)(collection.breakOut).distinct
        recordsAll.size -> recordsAll.slice(offset, size)
      }
    }

    val q = getParam("q")

    q.fold(failure, success)
  }
}
