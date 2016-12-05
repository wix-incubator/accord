import com.typesafe.sbt.pgp.PgpKeys._

lazy val javaRuntimeVersion = settingKey[ Double ]( "The JVM runtime version (e.g. 1.8)" )

lazy val publishSettings = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if ( version.value.trim.endsWith( "SNAPSHOT" ) )
      Some( "snapshots" at nexus + "content/repositories/snapshots" )
    else
      Some( "releases" at nexus + "service/local/staging/deploy/maven2" )
  },
  publishMavenStyle := true,
  credentials in Scaladex += Credentials( Path.userHome / ".ivy2" / ".scaladex.credentials" ),
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

def noFatalWarningsOn( task: sbt.TaskKey[_] = compile, configuration: sbt.Configuration = Compile ) =
  task match {
    case `compile` =>
      scalacOptions in configuration ~= { _ filterNot { _ == "-Xfatal-warnings" } }

    case _ =>
      scalacOptions in ( configuration, task ) :=
        ( scalacOptions in ( Compile, compile ) ).value filterNot { _ == "-Xfatal-warnings" }
  }

lazy val compileOptions = Seq(
  scalaVersion := "2.11.1",
  javaRuntimeVersion := System.getProperty( "java.vm.specification.version" ).toDouble,
  crossScalaVersions := ( javaRuntimeVersion.value match {
    case v if v >= 1.8 => Seq( "2.11.1", "2.12.0" )
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
)

lazy val baseSettings =
  publishSettings ++
  releaseSettings ++
  compileOptions ++
  sbtdoge.CrossPerProjectPlugin.projectSettings ++
  Seq(
    organization := "com.wix",
    homepage := Some( url( "https://github.com/wix/accord" ) ),
    licenses := Seq( "Apache-2.0" -> url( "http://www.opensource.org/licenses/Apache-2.0" ) )
  )

lazy val noPublish = Seq( publish := {}, publishLocal := {}, publishArtifact := false )

// Necessary to work around scala/scala-dev#275 (see wix/accord#84)
def providedScalaCompiler =
  libraryDependencies <+= scalaVersion { "org.scala-lang" % "scala-compiler" % _ % "provided" }

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
  .jsSettings(
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  )
  .jvmSettings(
    libraryDependencies <+= scalaVersion {
      case v if v startsWith "2.12" => "org.scalatest" %% "scalatest" % "3.0.0" % "test"
      case _ => "org.scalatest" %% "scalatest" % "2.2.6" % "test"
    }
  )

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
      noFatalWarningsOn( configuration = Test )
    ) :_* )
  .jsSettings(
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0"
  )
  .jvmSettings(
    libraryDependencies <+= scalaVersion {
      case v if v startsWith "2.12" => "org.scalatest" %% "scalatest" % "3.0.0"
      case _ => "org.scalatest" %% "scalatest" % "2.2.6"
    }
  )
lazy val scalatestJVM = scalatest.jvm
lazy val scalatestJS = scalatest.js

lazy val specs2 =
  Project(
    id = "specs2",
    base = file( "specs2" ),
    settings = baseSettings ++ Seq(
      name := "accord-specs2",
      libraryDependencies <+= scalaVersion {
        case v if v startsWith "2.12" => "org.specs2" %% "specs2-core" % "3.8.6"
        case _ => "org.specs2" %% "specs2-core" % "3.6.5"
      },
      noFatalWarningsOn( compile, Test )
    )
  ).dependsOn( apiJVM )

lazy val core =
  crossProject
    .crossType( CrossType.Pure )
    .in( file( "core" ) )
    .dependsOn( api, scalatest % "test->compile" )
    .settings( Seq(
      name := "accord-core",

      libraryDependencies += "org.scalamacros" %% "resetallattrs" % "1.0.0",
      libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-reflect" % _ % "provided" ),
      libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-compiler" % _ % "provided" ),

      description :=
        "Accord is a validation library written in and for Scala. Its chief aim is to provide a composable, " +
        "dead-simple and self-contained story for defining validation rules and executing them on object " +
        "instances. Feedback, bug reports and improvements are welcome!"
    ) ++ baseSettings :_* )
lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val java8 =
  crossProject
  .crossType( CrossType.Pure )
  .in( file( "java8" ) )
  .dependsOn( api, scalatest % "test->compile" )
  .settings( Seq(
    name := "accord-java8",
    description := "Adds native Accord combinators for Java 8 features"
  ) ++ baseSettings :_* )
lazy val java8JVM = java8.jvm
lazy val java8JS = java8.js

lazy val joda =
  crossProject
    .crossType( CrossType.Pure )
    .in( file( "joda" ) )
    .dependsOn( api, scalatest % "test->compile" )
    .settings( Seq(
      name := "accord-joda",
      libraryDependencies += "joda-time" % "joda-time" % "2.9.6",
      description := "Adds native Accord combinators for Joda-Time"
    ) ++ baseSettings :_* )
lazy val jodaJVM = joda.jvm
lazy val jodaJS = joda.js

lazy val spring3 =
  Project(
    id = "spring3",
    base = file ( "spring3" ),
    settings = baseSettings ++ providedScalaCompiler
  )
  .dependsOn( apiJVM, scalatestJVM % "test->compile", coreJVM % "test->compile" )

lazy val examples =
  Project(
    id = "examples",
    base = file( "examples" ),
    settings = baseSettings ++ noPublish ++ providedScalaCompiler ++ Seq(
      name := "accord-examples",
      description := "Sample projects for the Accord validation library.",
      noFatalWarningsOn( configuration = Compile )
    ) )
  .dependsOn( apiJVM, coreJVM, scalatestJVM % "test->compile", specs2 % "test->compile", spring3 )


// Root --

lazy val root =
  Project(
    id = "root",
    base = file( "." ),
    settings = baseSettings ++ noPublish
  )
  .aggregate(
    apiJVM, apiJS, coreJVM, coreJS,                 // Core modules
    scalatestJVM, scalatestJS, specs2, spring3,     // Testing support
    java8JVM, java8JS, jodaJVM, jodaJS,             // Optional modules
    examples                                        // Extras
  )
