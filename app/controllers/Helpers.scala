package controllers

import java.security.MessageDigest

object Helpers {
  def hash(salt: Array[Byte])(password: String): Array[Byte] = {
    val digest = MessageDigest.getInstance("SHA-256")
    digest.reset()
    digest.update(salt)
    digest.digest(password.getBytes("UTF-8"))
  }
}
