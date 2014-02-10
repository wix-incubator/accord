import sbt._
import Keys._

object Root extends Build {

  lazy val publishSettings = Seq(
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if ( version.value.trim.endsWith( "SNAPSHOT" ) )
        Some( "snapshots" at nexus + "content/repositories/snapshots" )
      else
        Some( "releases"  at nexus + "service/local/staging/deploy/maven2" )
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

  lazy val releaseSettings = {
    import sbtrelease.ReleaseStep
    import sbtrelease.ReleasePlugin.ReleaseKeys._
    import sbtrelease.ReleaseStateTransformations._
    import com.typesafe.sbt.pgp.PgpKeys._

    // Hook up release and GPG plugins
    lazy val publishSignedAction = { st: State =>
      val extracted = Project.extract( st )
      val ref = extracted.get( thisProjectRef )
      extracted.runAggregated( publishSigned in Global in ref, st )
    }

    sbtrelease.ReleasePlugin.releaseSettings ++ Seq(
      releaseProcess := Seq[ ReleaseStep ] (
        checkSnapshotDependencies,
        runTest,
        inquireVersions,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishArtifacts.copy( action = publishSignedAction ),
        setNextVersion,
        commitNextVersion,
        pushChanges
      )
    )
  }

  lazy val compileOptions = Seq(
    scalaVersion := "2.10.3",
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

  lazy val baseSettings = Project.defaultSettings ++ publishSettings ++ releaseSettings ++ compileOptions ++ Seq(
    organization := "com.wix",
    homepage := Some( url( "https://github.com/wix/accord" ) ),
    licenses := Seq( "Apache 2.0" -> url( "http://www.opensource.org/licenses/Apache-2.0" ) )
  )

  lazy val noPublish = Seq( publish := {}, publishLocal := {}, publishArtifact := false )

  // Projects --

  lazy val api = Project( id = "accord-api", base = file( "api" ), settings = baseSettings )
  lazy val scalatest = Project( id = "accord-scalatest", base = file( "scalatest" ), settings = baseSettings).dependsOn( api )
  lazy val specs2 = Project( id = "accord-specs2", base = file( "specs2" ), settings = baseSettings ).dependsOn( api )
  lazy val core = Project( id = "accord-core", base = file( "core" ), settings = baseSettings ).dependsOn( api, scalatest % "test->compile" )
  lazy val spring3 = Project( id = "accord-spring3", base = file ( "spring3" ), settings = baseSettings )
    .dependsOn( api, scalatest % "test->compile", core % "test->compile" )
  lazy val root = Project( id = "root", base = file( "." ), settings = baseSettings ++ noPublish )
    .aggregate( api, core, scalatest, specs2, spring3 )
}