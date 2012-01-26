import play.api._
import play.api.db._
import org.squeryl._
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import models.Files

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    val ds = DB.getDataSource("dev")(app)
    SessionFactory.concreteFactory =
      Some(() => Session.create(ds.getConnection, new H2Adapter))

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
