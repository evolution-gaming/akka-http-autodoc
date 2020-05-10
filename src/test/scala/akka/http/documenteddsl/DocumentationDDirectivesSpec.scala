package akka.http.documenteddsl

import akka.http.documenteddsl.directives.DocumentationDDirectives._
import akka.http.documenteddsl.documentation.RouteDocumentation
import matchers.must.Matchers._
import org.scalatest.matchers
import org.scalatest.wordspec.AnyWordSpec

class DocumentationDDirectivesSpec extends AnyWordSpec with DDirectivesSpec {

  "Category" must {
    "be applied for 1 segment" in {
      Category("xxx").describe(RouteDocumentation()).category mustBe Some(List("xxx"))
    }
    "be applied for N segments" in {
      Category("a", "b", "c").describe(RouteDocumentation()).category mustBe Some(List("a", "b", "c"))
    }
  }

  "Title" must {
    "be applied" in {
      Title("xxx").describe(RouteDocumentation()).title mustBe Some("xxx")
    }
  }

  "Description" must {
    "be applied" in {
      Description("xxx").describe(RouteDocumentation()).description mustBe Some("xxx")
    }
  }

}
