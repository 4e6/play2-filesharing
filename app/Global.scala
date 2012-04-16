import play.api._
import play.api.db.DB
import org.squeryl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.PostgreSqlAdapter
import lib.Config._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    implicit val a = app

    SessionFactory.concreteFactory = Some { () =>
      val s = Session.create(DB.getDataSource("default").getConnection, new PostgreSqlAdapter)
      if (logStatements_?) s.setLogger(Logger.debug(_))
      s
    }

    transaction {
      try {
        models.Storage.create
      } catch {
        case e @ (_: Exception | _: RuntimeException) =>
          Logger.warn("Failed to create models")
          Logger.error(e.toString)
      }
    }

    workers.Scheduler.start()

    Logger.info("Application configured")
  }

  override def onStop(app: Application) {
    workers.Scheduler.stop()
  }
}
