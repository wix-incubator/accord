organization in ThisBuild := "com.wix"

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.10.3"

publishTo in ThisBuild := {
  val nexus = "https://oss.sonatype.org/"
  if ( version.value.trim.endsWith( "SNAPSHOT" ) )
    Some( "snapshots" at nexus + "content/repositories/snapshots" )
  else
    Some( "releases"  at nexus + "service/local/staging/deploy/maven2" )
}

publishMavenStyle in ThisBuild := true

licenses in ThisBuild := Seq( "Apache 2.0" -> url( "http://www.opensource.org/licenses/Apache-2.0" ) )

homepage := Some( url( "https://github.com/holograph/accord" ) )

pomExtra in ThisBuild :=
  <scm>
    <url>git@github.com:holograph/accord.git</url>
    <connection>scm:git@github.com:holograph/accord.git</connection>
  </scm>
  <developers>
    <developer>
      <id>Holograph</id>
      <name>Tomer Gabel</name>
      <url>http://www.tomergabel.com</url>
    </developer>
  </developers>

scalacOptions in ThisBuild ++= Seq(
  "-language:reflectiveCalls",
  "-feature",
  "-deprecation",
  "-unchecked",
  "-Xfatal-warnings"
)

lazy val api = project in file( "api" )

lazy val scalatest = project in file( "scalatest" ) dependsOn( api )

lazy val specs2 = project in file( "specs2" ) dependsOn( api )

lazy val core = project in file( "core" ) dependsOn( api, scalatest % "test->compile" )

lazy val spring3 = project in file ( "spring3" ) dependsOn( api, scalatest % "test->compile", core % "test->compile" )

lazy val root = project in file( "." ) aggregate( api, core, scalatest, specs2, spring3 )

publishArtifact := false	// Only affects root
