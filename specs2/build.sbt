name := "accord-specs2"

libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-reflect" % _ % "provided" )

libraryDependencies += "org.specs2" %% "specs2" % "2.3.10"

description := "Specs² matchers for the Accord validation library"
