organization := "com.tomergabel"

name := "accord"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if ( version.value.trim.endsWith( "SNAPSHOT" ) )
    Some( "snapshots" at nexus + "content/repositories/snapshots" )
  else
    Some( "releases"  at nexus + "service/local/staging/deploy/maven2" )
}

libraryDependencies <+= scalaVersion( sv => "org.scala-lang" % "scala-reflect" % sv )

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

publishMavenStyle := true

description := "Accord is a validation library written in and for Scala. Its chief aim is to provide a composable, " +
               "dead-simple and self-contained story for defining validation rules and executing them on object " +
               "instances. Feedback, bug reports and improvements are welcome!"

pomExtra :=
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
