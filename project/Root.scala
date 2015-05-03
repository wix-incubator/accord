import sbt._
import Keys._

object Root extends Build {

  lazy val publishSettings = Seq(
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if ( version.value.trim.endsWith( "SNAPSHOT" ) )
        Some( "snapshots" at nexus + "content/repositories/snapshots" )
      else
        Some( "releases" at nexus + "service/local/staging/deploy/maven2" )
    },
    publishMavenStyle := true,
    pomExtra in ThisBuild :=
      <scm>
        <url>git@github.com:wix/accord.git</url>
        <connection>scm:git@github.com:wix/accord.git</connection>
      </scm>
      <developers>
        <developer>
          <id>Holograph</id>
          <name>Tomer Gabel</name>
          <url>http://www.tomergabel.com</url>
        </developer>
      </developers>
  )

  lazy val compileOptions = Seq(
    scalaVersion := "2.11.1",
    crossScalaVersions := Seq( "2.10.3", "2.11.1" ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Xfatal-warnings"
    ),

    // Warnings aren't considered fatal on document generation. There's probably a cleaner way to do this
    scalacOptions in ( Compile, doc ) :=
      ( scalacOptions in ( Compile, compile ) ).value filterNot { _ == "-Xfatal-warnings" }
  )

  lazy val baseSettings =
    sbtrelease.ReleasePlugin.releaseSettings ++
    publishSettings ++
    compileOptions ++
    Seq(
      organization := "com.wix",
      homepage := Some( url( "https://github.com/wix/accord" ) ),
      licenses := Seq( "Apache-2.0" -> url( "http://www.opensource.org/licenses/Apache-2.0" ) )
    )

  lazy val noPublish = Seq( publish := {}, publishLocal := {}, publishArtifact := false )

  // Projects --

  val specs2_2xSettings = Seq(
    name := "accord-specs2",
    libraryDependencies += "org.specs2" %% "specs2" % "2.3.13",
    target <<= target { _ / "specs2-2.x" }
  )

  val specs2_3xSettings = Seq(
    name := "accord-specs2-3.x",
    libraryDependencies += "org.specs2" %% "specs2-core" % "3.6",
    resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
    target <<= target { _ / "specs2-3.x" }
  )

  lazy val api =
    Project( id = "accord-api", base = file( "api" ), settings = baseSettings )
  lazy val scalatest =
    Project( id = "accord-scalatest", base = file( "scalatest" ), settings = baseSettings )
      .dependsOn( api )
  lazy val specs2_2x =
    Project( id = "accord-specs2-2x", base = file( "specs2" ), settings = baseSettings ++ specs2_2xSettings )
      .dependsOn( api )
  lazy val specs2_3x =
    Project( id = "accord-specs2-3x", base = file( "specs2" ), settings = baseSettings ++ specs2_3xSettings )
      .dependsOn( api )
  lazy val core =
    Project( id = "accord-core", base = file( "core" ), settings = baseSettings )
      .dependsOn( api, scalatest % "test->compile" )
  lazy val spring3 =
    Project( id = "accord-spring3", base = file ( "spring3" ), settings = baseSettings )
      .dependsOn( api, scalatest % "test->compile", core % "test->compile" )
  lazy val examples =
    Project( id = "accord-examples", base = file( "examples" ), settings = baseSettings ++ noPublish )
      .dependsOn( api, core, scalatest % "test->compile", specs2_2x % "test->compile", spring3 )
  lazy val root =
    Project( id = "root", base = file( "." ), settings = baseSettings ++ noPublish )
      .aggregate( api, core, scalatest, specs2_2x, specs2_3x, spring3, examples )
}
