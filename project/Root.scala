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
    publishSettings ++
    releaseSettings ++
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

  import org.scalajs.sbtplugin.cross._
  import CrossProject._


  lazy val api =
    crossProject.crossType( CrossType.Pure ).in( file( "api" ) ).settings( baseSettings :_* )
  lazy val apiJVM = api.jvm
  lazy val apiJS = api.js

  lazy val scalatest =
    crossProject.crossType( CrossType.Pure ).in( file( "scalatest" ) ).settings( baseSettings :_* )
      .dependsOn( api )
  lazy val scalatestJVM = scalatest.jvm
  lazy val scalatestJS = scalatest.js

  lazy val specs2_2x =
    crossProject.crossType( CrossType.Pure ).in( file( "api" ) ).settings( baseSettings ++ specs2_2xSettings :_* )
      .dependsOn( api )
  lazy val specs2_2xJVM = specs2_2x.jvm
  lazy val specs2_2xJS = specs2_2x.js

  lazy val specs2_3x =
    crossProject.crossType( CrossType.Pure ).in( file( "specs2" ) ).settings( baseSettings ++ specs2_3xSettings :_* )
      .dependsOn( api )
  lazy val specs2_3xJVM = specs2_3x.jvm
  lazy val specs2_3xJS = specs2_3x.js

  lazy val core =
    crossProject.crossType( CrossType.Pure ).in( file( "core" ) ).settings( baseSettings :_* )
      .dependsOn( api, scalatest % "test->compile" )
  lazy val coreJVM = core.jvm
  lazy val coreJS = core.js

  lazy val spring3 =
    Project( id = "accord-spring3", base = file ( "spring3" ), settings = baseSettings )
      .dependsOn( apiJVM, scalatestJVM % "test->compile", coreJVM % "test->compile" )

  lazy val examples =
    Project( id = "accord-examples", base = file( "examples" ), settings = baseSettings ++ noPublish )
      .dependsOn( apiJVM, coreJVM, scalatestJVM % "test->compile", specs2_2xJVM % "test->compile", spring3 )

  lazy val allProjectsJS: Seq[ ProjectReference ] = Seq( apiJS, coreJS, scalatestJS )
  lazy val allProjectsJVM: Seq[ ProjectReference ] = Seq( apiJVM, coreJVM, scalatestJVM, specs2_2xJVM, specs2_3xJVM, spring3, examples )
  lazy val allProjects = allProjectsJS ++ allProjectsJVM

  lazy val root =
    Project( id = "root", base = file( "." ), settings = baseSettings ++ noPublish )
      .aggregate( allProjects :_* )

  override def projects = super.projects map { _.enablePlugins( org.scalajs.sbtplugin.ScalaJSPlugin ) }   // Ugh.
}
