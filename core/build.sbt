name := "accord-core"

addCompilerPlugin( "org.scalamacros" % "paradise" % "2.0.0-M3" cross CrossVersion.full )

libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-reflect" % _ % "provided" )

libraryDependencies += "org.scalamacros" % "quasiquotes" % "2.0.0-M3" cross CrossVersion.full intransitive()

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

description := "Accord is a validation library written in and for Scala. Its chief aim is to provide a composable, " +
               "dead-simple and self-contained story for defining validation rules and executing them on object " +
               "instances. Feedback, bug reports and improvements are welcome!"
