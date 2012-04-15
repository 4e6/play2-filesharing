package controllers

import scalaz.{ Logger => _, _ }
import Scalaz._

import play.api.Logger
import play.api.mvc._

import models._
import lib.Helpers._

import scala.xml._

object Application extends Controller with ScalateEngine
  with Upload with Download {

  def index = Action {
    val params = Map(
      "menu" -> Set("Home"),
      "filesizeLimit" -> lib.Config.filesizeLimit.toString,
      "readableFilesize" -> readableSize(lib.Config.filesizeLimit))
    Ok(render("views/index.jade", params))
  }

  def pagination(page: Int,
                 pages: Int,
                 query: String = "",
                 url: String = "http://localhost:9000/search") = {
    def href(p: String) = url + "?q=" + query + ";p=" + p
    val delim = "..."
    val range = (1 to pages)
    val window = (page - 1) to (page + 1) filter { range contains }

    val l = window.head match {
      case 1 => Nil
      case 2 => "1" :: Nil
      case _ => "1" :: delim :: Nil
    }
    val r = window.last match {
      case p if p == pages => Nil
      case p if p == pages - 1 => pages.toString :: Nil
      case _ => delim :: pages.toString :: Nil
    }
    val ps = l ++ window.map(_.toString) ++ r

    val ls = ps.map { p =>
      val a = if (p == delim || p == page.toString)
        <a>{ p }</a>
      else <a href={ href(p) }>{ p }</a>
      val li = p match {
        case p if p == page.toString => "active"
        case p if p == delim => "disabled"
        case _ => "page"
      }
      <li class={ li }>{ a }</li>
    }

    <ul>{ ls }</ul>
  }

  def search(p: Int) = Action { implicit request =>
    Logger.info("search body[" + request.queryString + "]")

    val menu: Map[String, Any] = Map("menu" -> Set("Search"))

    val pageLength = 1

    def failure: NonEmptyList[String] => Result = _ =>
      Ok(render("views/search.jade", menu))
    def success: String => Result = { q =>
      val query = "%" + q + "%"
      val (size, results) = getRecords(query, (p - 1) * pageLength, pageLength)

      Logger.debug("results[" + results + "]")
      val params = menu ++ Map(
        "records" -> results,
        "page" -> p,
        "pages" -> (size / pageLength),
        "query" -> q,
        "resultsNumber" -> size)

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
