package workers

import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import akka.util.duration._

import models._
import lib.Helpers._

object Scheduler {

  lazy val scheduledTask = Akka.system.scheduler.schedule(10 seconds, 1 minute)(job)

  def job() {
    import org.squeryl.PrimitiveTypeMode._

    val now: java.sql.Timestamp = timeNow

    val urls = transaction {
      from(Storage.schedule)(task =>
        where(task.deletionTime lte now)
          select (task.url)
      ) toList
    }

    val deletedUrls = urls filter { url =>
      val dir = Storage.root / url
      val (deleted, remains) = dir.deleteRecursively(continueOnFailure = true)
      val msg = "Deleting " + dir + " [" + deleted + "," + remains + "]"
      if (deleted == 2) Logger.debug(msg) else Logger.error(msg)
      remains == 0
    }

    transaction(Storage.records deleteWhere { _.url in deletedUrls })
    transaction(Storage.schedule deleteWhere { _.url in deletedUrls })

  }

  def start() {
    scheduledTask
    Logger.info("Scheduler started")
  }

  def stop() {
    scheduledTask.cancel()
    Logger.info("Scheduler stopped")
  }
}
