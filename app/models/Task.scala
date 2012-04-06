package models

import org.squeryl.KeyedEntity
import java.sql.Timestamp

class Task(val url: String,
           val deletionTime: Timestamp)
  extends KeyedEntity[String] {

  def id = url
}
