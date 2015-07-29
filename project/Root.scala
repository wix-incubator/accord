import sbt.Keys._
import sbt._

// Plugin imports
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.SbtPgp.autoImport.PgpKeys._

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

  lazy val releaseSettings = Seq(
    releaseCrossBuild := true,
    releasePublishArtifactsAction := publishSigned.value
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

  lazy val api =
    crossProject
      .crossType( CrossType.Pure )
      .in( file( "api" ) )
      .settings( Seq(
        name := "accord-api",
        description :=
          "Accord is a validation library written in and for Scala. Its chief aim is to provide a composable, " +
          "dead-simple and self-contained story for defining validation rules and executing them on object " +
          "instances. Feedback, bug reports and improvements are welcome!"
      ) ++ baseSettings :_* )

  lazy val apiJVM = api.jvm
  lazy val apiJS = api.js

  lazy val scalatest =
    crossProject
      .crossType( CrossType.Pure )
      .in( file( "scalatest" ) )
      .dependsOn( api )
      .settings( Seq(
        name := "accord-scalatest",
        description := "ScalaTest matchers for the Accord validation library"
      ) ++ baseSettings :_* )
      .jvmSettings( libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" )
      .jsSettings( libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0-M7" )
  lazy val scalatestJVM = scalatest.jvm
  lazy val scalatestJS = scalatest.js

  lazy val specs2_2x =
    Project(
      id = "specs2_2x",
      base = file( "specs2" ),
      settings = baseSettings ++ Seq(
        name := "accord-specs2",
        libraryDependencies += "org.specs2" %% "specs2" % "2.3.13",
        target <<= target { _ / "specs2-2.x" }
      )
    ).dependsOn( apiJVM )

  lazy val specs2_3x =
    Project(
      id = "specs2_3x",
      base = file( "specs2" ),
      settings = baseSettings ++ Seq(
        name := "accord-specs2-3.x",
        libraryDependencies += "org.specs2" %% "specs2-core" % "3.6",
        resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
        target <<= target { _ / "specs2-3.x" }
      )
    ).dependsOn( apiJVM )

  lazy val macroDependencies =
    scalaVersion {
      case v if v startsWith "2.10" =>
        Seq(
          compilerPlugin( "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full ),
          "org.scalamacros" %% "quasiquotes" % "2.0.1" intransitive()
        )

      case v if v startsWith "2.11" =>
        Seq( "org.scalamacros" %% "resetallattrs" % "1.0.0-M1" )

      case _ => Seq.empty
    }

  lazy val core =
    crossProject
      .crossType( CrossType.Pure )
      .in( file( "core" ) )
      .dependsOn( api, scalatest % "test->compile" )
      .settings( Seq(
        name := "accord-core",

        libraryDependencies <++= macroDependencies,
        libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-reflect" % _ % "provided" ),
        libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-compiler" % _ % "provided" ),

        unmanagedSourceDirectories in Compile <+= ( scalaVersion, baseDirectory ) {
          case ( v, base ) if v startsWith "2.10" => base.getParentFile / "src/main/scala-2.10"
          case ( v, base ) if v startsWith "2.11" => base.getParentFile / "src/main/scala-2.11"
          case ( v, _ ) => throw new IllegalStateException( s"Unsupported Scala version $v" )
        },

        description :=
          "Accord is a validation library written in and for Scala. Its chief aim is to provide a composable, " +
          "dead-simple and self-contained story for defining validation rules and executing them on object " +
          "instances. Feedback, bug reports and improvements are welcome!"
      ) ++ baseSettings :_* )
  lazy val coreJVM = core.jvm
  lazy val coreJS = core.js

  lazy val spring3 =
    Project( id = "spring3", base = file ( "spring3" ), settings = baseSettings )
      .dependsOn( apiJVM, scalatestJVM % "test->compile", coreJVM % "test->compile" )

  lazy val examples =
    Project(
      id = "examples",
      base = file( "examples" ),
      settings = baseSettings ++ noPublish ++ Seq(
        name := "accord-examples",
        libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-compiler" % _ % "provided" ),
        libraryDependencies ++= Seq( "org.scalatest" %% "scalatest" % "2.1.3" % "test" ),
        description := "Sample projects for the Accord validation library."
      ) )
      .dependsOn( apiJVM, coreJVM, scalatestJVM % "test->compile", specs2_2x % "test->compile", spring3 )


  // Root --

  lazy val root =
    Project(
      id = "root",
      base = file( "." ),
      settings = baseSettings ++ noPublish
    )
    .aggregate( apiJVM, apiJS, coreJVM, coreJS, scalatestJVM, scalatestJS, specs2_2x, specs2_3x, spring3, examples )
}
