import play.api._
import play.api.db._
import org.squeryl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import models.Files

object Global extends GlobalSettings {
  def logStatements_?(implicit app: Application) =
    app.configuration.getBoolean("db.dev.logStatements") getOrElse false

  override def onStart(app: Application) {
    implicit val a = app

    SessionFactory.concreteFactory = Some { () =>
      val s = Session.create(DB.getDataSource("dev").getConnection, new H2Adapter)
      if (logStatements_?) s.setLogger(Logger.debug(_))
      s
    }

    transaction {
      try {
        Files.create
      } catch {
        case e @ (_: Exception | _: RuntimeException) =>
      }
    }

    Logger.info("Application configured")
  }
}
