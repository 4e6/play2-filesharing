package models

import org.squeryl.KeyedEntity
import java.sql.Timestamp

import lib.Helpers._

class Record(val url: String,
             val name: String,
             val size: Long,
             val creationTime: Timestamp,
             val deletionTime: Timestamp,
             val password: Option[Array[Byte]],
             val question: Option[String],
             val answer: Option[Array[Byte]])
  extends KeyedEntity[String] {

  def this() = this(
    "url",
    "name",
    0,
    0,
    0,
    Some(Array.empty),
    Some("question"),
    Some(Array.empty))

  def id = url

  def path = Storage.root / url / name

  def file = path.asInstanceOf[scalax.file.defaultfs.DefaultPath].jfile

  def readableSize = {
    val mask = "%.1f"
    def convert(size: Double, px: Seq[String]): String = {
      val next = size / 1024
      if (px.nonEmpty && next > 1) convert(next, px.tail)
      else mask.format(size) + " " + px.head
    }

    convert(size, bytePrefixes)
  }

  def timeLeft = {
    import akka.util.duration._
    val millisLeft = deletionTime.getTime - timeNow
    val minutes = millisLeft.toMinutes - millisLeft.toHours.hours.toMinutes
    val hours = millisLeft.toHours - millisLeft.toDays.days.toHours
    val days = millisLeft.toDays

    minutes :: hours :: days :: Nil
  }
}

object Record {
  import scalaz.{ Logger => _, _ }
  import Scalaz._

  import play.api.http.HeaderNames.CONTENT_LENGTH
  import play.api.mvc._
  import MultipartFormData.FilePart
  import play.api.libs.Files.TemporaryFile
  import lib.Config

  type V[X] = ValidationNEL[String, X]

  object File {
    def apply(implicit r: Request[MultipartFormData[TemporaryFile]]) =
      if (r.headers.get(CONTENT_LENGTH).map(_.toLong < Config.filesizeLimit) | false)
        r.body.file("file").toSuccess("file not found").liftFailNel
      else
        "file is too large".failNel
  }

  object URL {
    def valid(url: String) =
      if (url matches """^[^\s?&]+[^?&]*$""") url.successNel
      else "invalid url".failNel

    def available(url: String) =
      Record.get(url).toOption.cata(_ => "url reserved".failNel, url.successNel)

    def apply[T: Request](file: V[FilePart[_]]) = {
      val url = (getParam("url"), file) match {
        case (Success(u), _) => Success(u)
        case (_, Success(f)) => Success(f.filename)
        case (_, Failure(e)) => e.fail
      }

      url flatMap available
    }

    def apply[T: Request] = getParam("url") flatMap available

    def get[T: Request] = getParam("url")
  }

  object Question {
    def apply[T: Request] = getParam("question")
  }

  sealed trait Secret

  case object Password extends Secret {
    def apply[T: Request](time: Long) = getParam("password") map hash(time)

    def get[T: Request] = getParam("password")

    def verify(r: Record, p: String) =
      if (r.password | Array.empty sameElements hash(r.creationTime.getTime)(p))
        r.successNel
      else
        "incorrect password".failNel
  }

  case object Answer {
    def apply[T: Request](time: Long) = getParam("answer") map hash(time)

    def get[T: Request] = getParam("answer")

    def verify(r: Record, a: String) =
      if (r.answer | Array.empty sameElements hash(r.creationTime.getTime)(a))
        r.successNel
      else
        "incorrect answer".failNel
  }

  object Secret {
    def apply(password: V[_], answer: V[_]) = (password, answer) match {
      case (Failure(_), Success(a)) => Answer
      case _ => Password
    }
  }

  private[this] def prepare(file: FilePart[TemporaryFile], url: String) = {
    val FilePart(_, name, _, ref) = file
    val size = ref.file.length
    val dest = Storage.root / url / name

    /* Workaround for scala-io 'moveTo bug
       * https://github.com/jesseeichar/scala-io/issues/54*/
    scalax.file.Path(ref.file) copyTo dest
    ref.clean

    name -> size
  }

  def apply(file: V[FilePart[TemporaryFile]],
            url: V[String],
            password: V[Array[Byte]],
            question: V[String],
            answer: V[Array[Byte]],
            from: Long) = {
    val to = from + lib.Config.storageTime
    Secret(password, answer) match {
      case Password => (file |@| url |@| password) { (f, u, p) =>
        val (name, size) = prepare(f, u)
        new Record(u, name, size, from, to, Some(p), None, None)
      }
      case Answer => (file |@| url |@| question |@| answer) { (f, u, q, a) =>
        val (name, size) = prepare(f, u)
        new Record(u, name, size, from, to, None, Some(q), Some(a))
      }
    }
  }

  def get(url: String): V[Record] = {
    import org.squeryl.PrimitiveTypeMode._
    transaction(models.Storage.records lookup url)
      .toSuccess("file " + url + " not found").liftFailNel
  }

  def get[T: Request]: V[Record] = URL.get flatMap Record.get

  def verify(record: V[Record],
             password: V[String],
             answer: V[String]) =
    Secret(password, answer) match {
      case Password => (record <|*|> password) flatMap { Password.verify _ tupled }
      case Answer => (record <|*|> answer) flatMap { Answer.verify _ tupled }
    }
}
