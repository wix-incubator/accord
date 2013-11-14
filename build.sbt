organization in ThisBuild := "com.tomergabel"

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

pomExtra in ThisBuild :=
  <url>https://github.com/holograph/accord</url>
  <licenses>
    <license>
      <name>Apache</name>
      <url>http://www.opensource.org/licenses/Apache-2.0</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:holograph/accord.git</url>
    <connection>scm:git@github.com:holograph/accord.git</connection>
  </scm>
  <developers>
    <developer>
      <id>Holgoraph</id>
      <name>Tomer Gabel</name>
      <url>http://www.tomergabel.com</url>
    </developer>
  </developers>

scalacOptions in ThisBuild ++= Seq( "-feature", "-language:reflectiveCalls" )

lazy val core = project in file( "core" )

lazy val root = project in file( "." ) aggregate( core )

publishArtifact := false	// Only affects root
