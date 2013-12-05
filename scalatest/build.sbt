name := "accord-scalatest"

libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-reflect" % _ % "provided" )

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0"

description := "ScalaTest matchers for the Accord validation library"
