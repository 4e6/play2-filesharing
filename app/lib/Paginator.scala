package lib

import scalaz._
import Scalaz._

class Paginator(width: Int = 3,
                url: String = "http://localhost:9000/search",
                delimiter: String = "...") {
  private case class Page(val num: String,
                          val aHref: Option[String] = None,
                          val liClass: Option[String] = None) {
    def withHref(href: String) = Page(num, aHref = Some(href))
    def withClass(c: String) = Page(num, liClass = Some(c))
  }

  private def markup(ps: List[Page]) = {
    val ls = ps map { p =>
      val a = p.aHref.map(h => <a href={ h }>{ p.num }</a>) | <a>{ p.num }</a>
      p.liClass.map(c => <li class={ c }>{ a }</li>) | <li>{ a }</li>
    }
    <ul>{ ls }</ul>
  }

  def paginate(page: Int, total: Int, query: String) = {
    val || = delimiter
    def href(page: Int) = url + "?q=" + query + ";p=" + page

    val window = (page - width / 2).max(1) to (page + width / 2).min(total)

    val left = window.head match {
      case 1 => Nil
      case 2 => Right(1) :: Nil
      case _ => Right(1) :: Left(||) :: Nil
    }

    val right = window.last match {
      case p if p == total => Nil
      case p if p == total - 1 => Right(total) :: Nil
      case _ => Left(||) :: Right(total) :: Nil
    }

    val ps = (left ++ window.map(Right apply _) ++ right) map {
      case Right(n) if n == page => Page(n.toString) withClass "active"
      case Right(n) => Page(n.toString) withHref href(n)
      case Left(d) => Page(d) withClass "disabled"
    }

    markup(ps)
  }
}

object Paginator {
  def apply(page: Int, total: Int, query: String) = new Paginator().paginate(page, total, query)
}
