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

  def search(page: Int) = Action { implicit request =>
    val menu: Map[String, Any] = Map("menu" -> Set("Search"))

    def failure: NonEmptyList[String] => Result = _ =>
      Ok(render("views/search.jade", menu))
    def success: String => Result = { q =>
      val query = "%" + q + "%"
      val results = getRecords(query, page, 50)

      Logger.debug("results[" + results + "]")
      val params = Map("records" -> results)

      Ok(render("views/search.jade", menu ++ params))
    }

    def getRecords(name: String, offset: Int, size: Int) = {
      import org.squeryl.PrimitiveTypeMode._
      transaction {
        from(Storage.records)(r =>
          where(r.url like name)
            select (r)
            orderBy (r.creationTime asc)
        ).page(offset, size).toList
      }
    }

    val q = getParam("q")

    q.fold(failure, success)
  }
}
