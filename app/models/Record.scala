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

  def timeLeft = deletionTime.getTime - timeNow
}

object Record {
  import scalaz._
  import Scalaz._

  import play.api.http.HeaderNames.CONTENT_LENGTH
  import play.api.mvc._
  import MultipartFormData.FilePart
  import play.api.libs.Files.TemporaryFile
  import lib._

  type V[X] = ValidationNEL[String, X]

  trait RequestParam {
    val name: String
    def get[_: Request] = getParam(name) flatMap InputValidator(name)
  }

  object File {
    def apply(implicit r: Request[MultipartFormData[TemporaryFile]]) =
      if (r.headers.get(CONTENT_LENGTH).map(_.toLong < Config.filesizeLimit) | false)
        r.body.file("file").toSuccess("file not found").liftFailNel
      else
        "file is too large".failNel
  }

  object URL extends RequestParam {
    val name = "url"
    def available(url: String) =
      Record.get(url).toOption.cata(_ => "%s reserved".format(name).failNel, url.successNel)

    def apply[T: Request](file: V[FilePart[_]] = "file not provided".failNel) = {
      val url: V[String] = (getParam(name), file) match {
        case (Success(u), _) => Success(u)
        case (_, Success(f)) => Success(f.filename)
        case (_, Failure(e)) => Failure(e)
      }

      url flatMap InputValidator(name) flatMap available
    }
  }

  object Question extends RequestParam {
    val name = "question"
    def apply[T: Request] = get
  }

  sealed trait Secret extends RequestParam {
    def secret(r: Record): Option[Array[Byte]]

    def verify(r: Record, s: String) =
      (secret(r) | Array.empty sameElements hash(r.creationTime.getTime)(s)) ?
        r.successNel[String] | "incorrect %s".format(name).failNel

    def apply[T: Request](time: Long) = get ∘ hash(time)
  }

  case object Password extends Secret {
    val name = "password"
    def secret(r: Record) = r.password
  }

  case object Answer extends Secret {
    val name = "answer"
    def secret(r: Record) = r.answer
  }

  object Secret {
    def apply(password: V[_]) = password.isFailure ? (Answer: Secret) | Password
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
    val to = from + Config.storageTime
    Secret(password) match {
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
      .toSuccess("file %s not found" format url).liftFailNel
  }

  def get[T: Request]: V[Record] = URL.get flatMap Record.get

  def verify(record: V[Record],
             password: V[String],
             answer: V[String]) =
    Secret(password) match {
      case Password => (record <|*|> password) flatMap { Password.verify _ tupled }
      case Answer => (record <|*|> answer) flatMap { Answer.verify _ tupled }
    }
}
