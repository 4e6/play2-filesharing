package lib

import scalaz._
import Scalaz._

import lib.Config.maxInputSize

class InputValidator(key: String, value: String) {

  val empty = validation(value.isEmpty either "%s is empty".format(key) or value)

  val maxSize = validation((value.size > maxInputSize) either "%s is too large".format(key) or value)

  val list = empty :: maxSize :: Nil

  def validate = list ∘ { _.liftFailNel } reduce { _ <* _ }
}

object InputValidator {
  def apply(k: String)(v: String) = new InputValidator(k, v) validate
}

