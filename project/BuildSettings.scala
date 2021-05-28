import java.net.URL

import sbt._
import sbt.Keys._
import bintray.BintrayPlugin.autoImport.bintrayOrganization
import sbtrelease.ReleasePlugin.autoImport.releaseCrossBuild

object BuildSettings {
  val artifactId = "akka-http-documenteddsl"

  lazy val basicSettings = Seq(
    name                  := artifactId,
    organization          := "com.evolutiongaming",
    homepage              := Some(new URL("http://github.com/evolution-gaming/akka-http-documenteddsl")),
    startYear             := Some(2016),
    organizationName      := "Evolution Gaming",
    organizationHomepage  := Some(url("http://evolutiongaming.com")),
    bintrayOrganization   := Some("evolutiongaming"),
    scalaVersion          := crossScalaVersions.value.head,
    crossScalaVersions    := Seq("2.13.3", "2.12.14"),
    releaseCrossBuild     := true,
    licenses              := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))))
}