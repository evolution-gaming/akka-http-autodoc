package akka.http.documenteddsl

import java.time.LocalDate

import akka.http.documenteddsl.DDirectives._
import akka.http.documenteddsl.documentation._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import matchers.must.Matchers._
import play.api.libs.json.Json
import org.scalatest.matchers
import org.scalatest.wordspec.AnyWordSpec

class PathDDirectivesSpec extends AnyWordSpec with DDirectivesSpec with ScalatestRouteTest {

  "PathPrefix" must {
    "be applied for 1 segment" in {
      PathPrefix("xxx").describe(RouteDocumentation()).path mustBe PathDocumentation.NonEmpty(prefix = Some("xxx"))
    }
    "be applied for N segments" in {
      PathPrefix("a" / "b" / "c").describe(RouteDocumentation()).path mustBe PathDocumentation.NonEmpty(prefix = Some("a/b/c"))
    }
    "mix by conjunction" in {
      (PathPrefix("foo") & Path("bar")).describe(RouteDocumentation()).path mustBe PathDocumentation.NonEmpty(prefix = Some("foo"), path = Some("bar"))
    }
    "mix by apply" in {
      val route = PathPrefix("foo") {
        Path("bar") { complete("") }
      }
      val doc = route.selfDescribe(Documentation()).routes
      doc.size mustBe 1
      doc.head.path mustBe PathDocumentation.NonEmpty(prefix = Some("foo"), path = Some("bar"))
    }
    "mix by apply hierarchically" in {
      val route = PathPrefix("foo") {
        PathPrefix("bar") {
          Path("baz") {complete("")}
        }
      }
      val doc = route.selfDescribe(Documentation()).routes
      doc.size mustBe 1
      doc.head.path mustBe PathDocumentation.NonEmpty(prefix = Some("foo/bar"), path = Some("baz"))
    }
    "mix by apply (N inner routes)" in {
      val route = PathPrefix("0") {
        PathPrefix("0") {
          Path("0") { complete("0") } |~|
          Path("1") { complete("1") }
        } |~|
        PathPrefix("1") {
          Path("0") { complete("0") } |~|
          Path("1") { complete("1") }
        }
      }
      val doc = route.selfDescribe(Documentation())
      doc.routes.size mustBe 4
      val List(r0, r1, r2, r3) = doc.routes
      r0.path mustBe PathDocumentation.NonEmpty(prefix = Some("0/0"), path = Some("0"))
      r1.path mustBe PathDocumentation.NonEmpty(prefix = Some("0/0"), path = Some("1"))
      r2.path mustBe PathDocumentation.NonEmpty(prefix = Some("0/1"), path = Some("0"))
      r3.path mustBe PathDocumentation.NonEmpty(prefix = Some("0/1"), path = Some("1"))
    }
    "mix by apply (NxN inner routes)" in {
      val route1 = PathPrefix("000") {
        Path("002") { complete("") } |~|
        Path("003") { complete("") }
      }
      val route2 = PathPrefix("111") {
        Path("112") { complete("") } |~|
        Path("113") { complete("") }
      }
      val doc = (route1 |~| route2) selfDescribe Documentation()
      val List(r0, r1, r2, r3) = doc.routes
      r0.path mustBe PathDocumentation.NonEmpty(prefix = Some("000"), path = Some("002"))
      r1.path mustBe PathDocumentation.NonEmpty(prefix = Some("000"), path = Some("003"))
      r2.path mustBe PathDocumentation.NonEmpty(prefix = Some("111"), path = Some("112"))
      r3.path mustBe PathDocumentation.NonEmpty(prefix = Some("111"), path = Some("113"))
    }
    "be translated to akka (N inner routes)" in {
      val route = PathPrefix("foo") {
        Path("bar") { complete("bar") } |~|
        Path("baz") { complete("baz") }
      }

      Get("/foo/bar") ~> route ~> check {handled mustBe true; responseAs[String] mustBe "bar"}
      Get("/foo/baz") ~> route ~> check {handled mustBe true; responseAs[String] mustBe "baz"}
    }
  }


  "PathSuffix" must {
    "be applied for 1 segment" in {
      PathSuffix("xxx").describe(RouteDocumentation()).path mustBe PathDocumentation.NonEmpty(suffix = Some("xxx"))
    }
    "be applied for N segments" in {
      PathSuffix("a" / "b" / "c").describe(RouteDocumentation()).path mustBe PathDocumentation.NonEmpty(suffix = Some("a/b/c"))
    }
    "mix by conjunction" in {
      (Path("bar") & PathSuffix("baz")).describe(RouteDocumentation()).path mustBe PathDocumentation.NonEmpty(path = Some("bar"), suffix = Some("baz"))
      (PathPrefix("foo") & Path("bar") & PathSuffix("baz")).describe(RouteDocumentation()).path mustBe PathDocumentation.NonEmpty(prefix = Some("foo"), path = Some("bar"), suffix = Some("baz"))
    }
    "mix by apply" in {
      val route = Path("bar") {
        PathSuffix("baz") { complete("") }
      }
      val doc = route.selfDescribe(Documentation()).routes
      doc.size mustBe 1
      doc.head.path mustBe PathDocumentation.NonEmpty(path = Some("bar"), suffix = Some("baz"))
    }
    "mix by apply (N inner routes)" in {
      val route = PathPrefix("foo") {
        Path("bar") {
          PathSuffix("baz") { complete("") } |~|
          PathSuffix("bax") { complete("") }
        }
      }
      val doc = route.selfDescribe(Documentation()).routes
      doc.size mustBe 2
      doc.head.path mustBe PathDocumentation.NonEmpty(prefix = Some("foo"), path = Some("bar"), suffix = Some("baz"))
      doc.tail.head.path mustBe PathDocumentation.NonEmpty(prefix = Some("foo"), path = Some("bar"), suffix = Some("bax"))
    }
  }

  "Path" must {
    "be applied for 1 segment" in {
      Path("xxx").describe(RouteDocumentation()).path mustBe PathDocumentation.NonEmpty(path = Some("xxx"))
    }
    "be applied for N segments" in {
      Path("a" / "b" / "c").describe(RouteDocumentation()).path mustBe PathDocumentation.NonEmpty(path = Some("a/b/c"))
    }
    "be applied for N segments with string variables" in {
      val doc = Path("a" / Segment[String]("user id") / "c").describe(RouteDocumentation())
      doc.path mustBe PathDocumentation.NonEmpty(path = Some("a/{string}/c"))
      doc.parameters mustBe Some(List(ParamDocumentation("user id", Json.obj("type" -> "string"), required = true, ParamDocumentation.Origin.Path)))
    }
    "be applied for N segments with number variables" in {
      val doc = Path("a" / Segment[Int]("user id") / "c").describe(RouteDocumentation())
      doc.path mustBe PathDocumentation.NonEmpty(path = Some("a/{number}/c"))
      doc.parameters mustBe Some(List(ParamDocumentation("user id", Json.obj("type" -> "number", "format" -> "number"), required = true, ParamDocumentation.Origin.Path)))
    }
    "be applied for N segments with date variables" in {
      val doc = Path("a" / Segment[LocalDate]("creation date") / "c").describe(RouteDocumentation())
      doc.path mustBe PathDocumentation.NonEmpty(path = Some("a/{date}/c"))
      doc.parameters mustBe Some(List(ParamDocumentation("creation date", Json.obj("type" -> "string", "format" -> "date", "pattern" -> "^[0-9]{4}-[0-9]{2}-[0-9]{2}$"), required = true, ParamDocumentation.Origin.Path)))
    }
    "be applied for N segments with RegExp variables" in {
      val doc = Path("a" / Segment.re("^[a-z0-9_\\.-]+@[\\da-z\\.-]+\\.[a-z\\.]{2,6}$".r, "email") / "c").describe(RouteDocumentation())
      doc.path mustBe PathDocumentation.NonEmpty(path = Some("a/{string}/c"))
      doc.parameters mustBe Some(List(ParamDocumentation("email", Json.obj("type" -> "string", "format" -> "regexp", "pattern" -> "^[a-z0-9_\\.-]+@[\\da-z\\.-]+\\.[a-z\\.]{2,6}$"), required = true, ParamDocumentation.Origin.Path)))
    }
    "be translated to akka (1 segment)" in {
      val route = Path("xxx") apply {complete("")}
      Get("/xxx") ~> route ~> check { handled mustBe true }
      Get("/yyy") ~> route ~> check { handled mustBe false }
    }
    "be translated to akka (2 segments)" in {
      val route = Path("a" / "b" / "c") apply {complete("")}
      Get("/a/b/c") ~> route ~> check {handled mustBe true}
      Get("/x/x") ~> route ~> check {handled mustBe false}
    }
    "be translated to akka (N segments with string variables)" in {
      val route = Path("a" / Segment[String]("user id") / "c") apply {x => complete(x)}
      Get("/a/b/c") ~> route ~> check {handled mustBe true; responseAs[String] mustBe "b"}
      Get("/x/x") ~> route ~> check {handled mustBe false}
    }
    "be translated to akka (N segments with int variables)" in {
      val route = Path("a" / Segment[Int]("user id") / "c") apply {x => complete(s"$x")}
      Get("/a/77/c") ~> route ~> check {handled mustBe true; responseAs[String] mustBe "77"}
      Get("/a/b/c") ~> route ~> check {handled mustBe false}
      Get("/x/x") ~> route ~> check {handled mustBe false}
    }
    "be translated to akka (N segments with date variables)" in {
      val route = Path("a" / Segment[LocalDate]("creation date") / "c") apply {(x: LocalDate) => complete(s"$x")}
      Get("/a/2000-01-01/c") ~> route ~> check {handled mustBe true; responseAs[String] mustBe "2000-01-01"}
      Get("/a/foo-bar/c") ~> route ~> check {handled mustBe false}
      Get("/x/x") ~> route ~> check {handled mustBe false}
    }
    "be translated to akka (N segments with RegExp variables)" in {
      val route = Path("a" / Segment.re("^[a-z0-9_\\.-]+@[\\da-z\\.-]+\\.[a-z\\.]{2,6}$".r) / "c") apply {x => complete(s"$x")}
      Get("/a/foo@bar.baz/c") ~> route ~> check {handled mustBe true; responseAs[String] mustBe "foo@bar.baz"}
      Get("/a/foo-bar/c") ~> route ~> check {handled mustBe false}
      Get("/x/x") ~> route ~> check {handled mustBe false}
    }
  }
}
