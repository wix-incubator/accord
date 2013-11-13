name := "accord"

version := "0.1"

scalaVersion := "2.10.3"

// resolvers += Resolver.sonatypeRepo( "releases" )

libraryDependencies <+= scalaVersion( sv => "org.scala-lang" % "scala-reflect" % sv )

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

