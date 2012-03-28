package workers

import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Akka
import akka.util.duration._

object Scheduler {

  lazy val scheduledTask =
    Akka.system.scheduler.schedule(1 second, 10 seconds) { job }
  
  def job() {
    Logger.info("Running scheduled job")
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
