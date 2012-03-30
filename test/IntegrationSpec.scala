package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

object IntegrationSpec extends Specification {

  "The sample test" should {
    "pass" in {
      true must beTrue
    }
  }

  "Application" should {
    "run in a browser" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333")
        browser.title must_== "Index title"
      }
    }
  }
}
