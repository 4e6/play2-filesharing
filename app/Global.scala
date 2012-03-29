import play.api._
import play.api.db.DB
import org.squeryl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import models.Config._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    implicit val a = app

    SessionFactory.concreteFactory = Some { () =>
      val s = Session.create(DB.getDataSource("dev").getConnection, new H2Adapter)
      if (logStatements_?) s.setLogger(Logger.debug(_))
      s
    }

    transaction {
      try {
        models.Storage.create
      } catch {
        case e @ (_: Exception | _: RuntimeException) =>
      }
    }

    workers.Scheduler.start()

    Logger.info("Application configured")
  }

  override def onStop(app: Application) {
    workers.Scheduler.stop()
  }
}
