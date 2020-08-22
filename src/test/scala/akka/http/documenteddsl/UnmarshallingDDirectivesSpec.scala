package akka.http.documenteddsl

import java.time.LocalDate

import akka.http.documenteddsl.directives.UnmarshallingDDirectives._
import akka.http.documenteddsl.documentation.OutDocumentation._
import akka.http.documenteddsl.documentation.{JsonSchema, OutDocumentation, RouteDocumentation}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import matchers.must.Matchers._
import play.api.libs.json.{Format, Json}
import org.scalatest.matchers
import org.scalatest.wordspec.AnyWordSpec

class UnmarshallingDDirectivesSpec extends AnyWordSpec with DDirectivesSpec with ScalatestRouteTest {
  import UnmarshallingDDirectivesSpec._

  "Out" must {
    val now = LocalDate.now()

    "be applied to route documentation" in {
      Out[TestOut].describe(RouteDocumentation()).out mustBe Some(OutDocumentation(
        success = List(
          Payload.Success(
            status = Status(StatusCodes.OK),
            contentType = "application/json",
            schema = JsonSchema.resolveSchema[TestOut],
            example = None))))
    }
    "be applied to route documentation (concatenated)" in {
      val out = Out(StatusCodes.Created, TestOut("id", Some("name"), now)) & Out(StatusCodes.NotFound, "entity not found")
      out.describe(RouteDocumentation()).out mustBe Some(OutDocumentation(
        failure = List(
          Payload.Failure(
            status = Status(StatusCodes.NotFound),
            contentType = None,
            description = Some("entity not found"))),
        success = List(
          Payload.Success(
            status = Status(StatusCodes.Created),
            contentType = "application/json",
            schema = JsonSchema.resolveSchema[TestOut],
            example = Some(Json toJson TestOut("id", Some("name"), now))))))
    }
  }

}

object UnmarshallingDDirectivesSpec {
  case class TestOut(id: String, name: Option[String], createdAt: LocalDate)
  implicit val testInFormat: Format[TestOut] = Json.format[TestOut]
}
