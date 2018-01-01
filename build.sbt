import com.typesafe.sbt.pgp.PgpKeys._
import Helpers._

lazy val publishSettings = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if ( version.value.trim.endsWith( "SNAPSHOT" ) )
      Some( "snapshots" at nexus + "content/repositories/snapshots" )
    else
      Some( "releases" at nexus + "service/local/staging/deploy/maven2" )
  },
  publishMavenStyle := true,
  scmInfo := Some( ScmInfo( url( "https://github.com/wix/accord" ), "scm:git:git@github.com:wix/accord.git" ) ),
  pomExtra in ThisBuild :=
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

def noFatalWarningsOn( task: sbt.TaskKey[_] = compile, configuration: sbt.Configuration = Compile ) =
  task match {
    case `compile` =>
      scalacOptions in configuration ~= { _ filterNot { _ == "-Xfatal-warnings" } }

    case _ =>
      scalacOptions in ( configuration, task ) :=
        ( scalacOptions in ( Compile, compile ) ).value filterNot { _ == "-Xfatal-warnings" }
  }

// Necessary to work around scala/scala-dev#275 (see wix/accord#84)
def providedScalaCompiler =
  libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"

def limitPackageSize( allowedSizeInKB: Int ) =
  packageBin in Compile := {
    val jar = ( packageBin in Compile ).value
    val sizeKB = jar.length() / 1024
    if ( sizeKB > allowedSizeInKB )
      sys.error( s"Resulting package $jar (size=${sizeKB}KB) is larger than the allowed limit of ${allowedSizeInKB}KB" )
    jar
  }

lazy val compileOptions = Seq(
  scalaVersion := "2.12.0",
  crossScalaVersions := ( Helpers.javaVersion match {
    case v if v >= 1.8 => Seq( "2.11.1", "2.12.0", "2.13.0-M2" )
    case _             => Seq( "2.11.1" )
  } ),
  scalacOptions ++= Seq(
    "-language:reflectiveCalls",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings"
  ),
  noFatalWarningsOn( task = doc )      // Warnings aren't considered fatal on document generation
) ++ providedScalaCompiler

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
        "instances. Feedback, bug reports and improvements are welcome!",
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.4" % "test",
      noFatalWarningsOn( configuration = Test )
    ) ++ baseSettings :_* )
  .jsSettings( limitPackageSize( 150 ) )
  .jvmSettings( limitPackageSize( 90 ) )

lazy val apiJVM = api.jvm
lazy val apiJS = api.js

lazy val scalatest =
  crossProject
    .crossType( CrossType.Pure )
    .in( file( "scalatest" ) )
    .dependsOn( api )
    .settings( baseSettings ++ Seq(
      name := "accord-scalatest",
      description := "ScalaTest matchers for the Accord validation library",
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.4",
      noFatalWarningsOn( configuration = Test )
    ) :_* )
  .jsSettings( limitPackageSize( 100 ) )
  .jvmSettings( limitPackageSize( 60 ) )
lazy val scalatestJVM = scalatest.jvm
lazy val scalatestJS = scalatest.js

lazy val specs2 =
  crossProject
    .crossType( CrossType.Pure )
    .in( file( "specs2" ) )
    .dependsOn( api )
    .settings( baseSettings ++ Seq(
      name := "accord-specs2",
      noFatalWarningsOn( compile, Test ),
      limitPackageSize( 80 )
    ) :_* )
    .jsSettings(
      limitPackageSize( 100 ),
      libraryDependencies += "org.specs2" %%% "specs2-core" % "4.0.2"
    )
    .jvmSettings(
      limitPackageSize( 60 ),
      libraryDependencies += { scalaVersion.value match {
        case v if v startsWith "2.13" => "org.specs2" %% "specs2-core" % "4.0.2"
        case _                        => "org.specs2" %% "specs2-core" % "3.8.6"
      } }
    )
lazy val specs2JVM = specs2.jvm
lazy val specs2JS = specs2.js

lazy val core =
  crossProject
    .crossType( CrossType.Pure )
    .in( file( "core" ) )
    .dependsOn( api, scalatest % "test->compile" )
    .settings( Seq(
      name := "accord-core",

      libraryDependencies += "org.scalamacros" %% "resetallattrs" % "1.0.0",
      libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided",

      description :=
        "Accord is a validation library written in and for Scala. Its chief aim is to provide a composable, " +
        "dead-simple and self-contained story for defining validation rules and executing them on object " +
        "instances. Feedback, bug reports and improvements are welcome!",

      noFatalWarningsOn( compile, Test )      // Avoid failed test compilation due to deprecations // TODO remove
    ) ++ baseSettings :_* )
    .jvmSettings( limitPackageSize( 500 ) )
    .jsSettings( limitPackageSize( 800 ) )
lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val java8 =
  crossProject
    .crossType( CrossType.Pure )
    .in( file( "java8" ) )
    .dependsOn( api, core, scalatest % "test->compile" )
    .settings( Seq(
      name := "accord-java8",
      description := "Adds native Accord combinators for Java 8 features",
      limitPackageSize( 30 )
    ) ++ baseSettings :_* )
    .jsSettings(
      // This library is still not complete (e.g. LocalDateTime isn't implemented); Scala.js support
      // for this module is consequently currently disabled.
      libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.0"
    )

lazy val java8JVM = java8.jvm
//lazy val java8JS = java8.js     // Disabled until scalajs-java-time comes along. See comment above

lazy val joda =
  project
    .in( file( "joda" ) )
    .settings( baseSettings :_* )
    .settings(
      name := "accord-joda",
      libraryDependencies ++= Seq(
        "joda-time" % "joda-time" % "2.9.7",
        "org.joda" % "joda-convert" % "1.8.1"  // Required for rendering constraints
      ),
      description := "Adds native Accord combinators for Joda-Time",
      limitPackageSize( 25 )
    )
  .dependsOn( apiJVM, coreJVM, scalatestJVM % "test->compile" )

lazy val spring3 =
  project
    .in( file ( "spring3" ) )
    .settings( baseSettings :_* )
    .settings( limitPackageSize( 25 ) )
    .whenJavaVersion( _ >= 1.9 ) { _.settings(
      libraryDependencies ++= Seq(
        "javax.xml.bind" % "jaxb-api" % "2.3.0",
        "javax.annotation" % "javax.annotation-api" % "1.3.1",
        "org.apache.commons" % "commons-lang3" % "3.5"
      )
    ) }
    .dependsOn( apiJVM, scalatestJVM % "test->compile", coreJVM % "test->compile" )

lazy val examples =
  project
    .in( file( "examples" ) )
    .settings( baseSettings :_* )
    .settings( noPublish :_* )
    .settings(
      name := "accord-examples",
      description := "Sample projects for the Accord validation library.",
      noFatalWarningsOn( configuration = Compile )
    )
    .dependsOn( apiJVM, coreJVM, scalatestJVM % "test->compile", specs2JVM % "test->compile", spring3 )


// Roots --

lazy val jvmRoot =
  project
    .settings( baseSettings :_* )
    .settings( noPublish :_* )
    .aggregate( apiJVM, coreJVM, scalatestJVM, specs2JVM, specs2JS, spring3, joda, examples )
    .whenJavaVersion( _ >= 1.8 ) { _.aggregate( java8JVM ) }

lazy val jsRoot =
  project
    .settings( baseSettings :_* )
    .settings( noPublish :_* )
    .aggregate( apiJS, coreJS, scalatestJS )
    //.whenJavaVersion( _ >= 1.8 ) { _.aggregate( java8JS ) }

lazy val root =
  project
    .in( file( "." ) )
    .settings( baseSettings :_* )
    .settings( noPublish :_* )
    .aggregate( jvmRoot, jsRoot )
