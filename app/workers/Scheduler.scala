package workers

import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import akka.util.duration._

object Scheduler {

  lazy val scheduledTask =
    Akka.system.scheduler.schedule(1 second, 1 minute) { job }

  def job() {
    import models._
    import controllers.Helpers._
    import scalax.file.Path
    import org.squeryl.PrimitiveTypeMode._
    Logger.info("Running scheduled job")

    def now = System.currentTimeMillis.timestamp
    def storageRoot = Path("/mnt/storage/webcb/files")

    val urls = transaction {
      from(Storage.schedule) { task =>
        where(task.deletionTime <= now)
        select(task.url)
      }
    }

    if (transaction(urls.nonEmpty)) {
      val files = transaction {
        from(Storage.files) { file =>
          where(file.url in urls)
          select(file)
        }
      }

      val deletedFiles = transaction {
        files filter { file =>
          val dir = Path(file.path).parent.get
          assert(dir.parent.get == storageRoot)
          Logger.info("Deleting: " + dir)
          val (deleted, remains) = dir.deleteRecursively()
          Logger.info("Deletion stats[" + deleted + "," + remains + "]")
          remains == 0
        }
      }
      val deletedUrls = deletedFiles.map(_.url)

      transaction(Storage.files deleteWhere { f => f.url in deletedUrls })
      transaction(Storage.schedule deleteWhere { t => t.url in deletedUrls })

    }
  }

  def start() {
    Logger.info("Scheduler started")
    scheduledTask
  }

  def stop() {
    Logger.info("Scheduler stopped")
    scheduledTask.cancel()
  }
}
