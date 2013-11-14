name := "accord"

libraryDependencies <+= scalaVersion( sv => "org.scala-lang" % "scala-reflect" % sv )

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

description := "Accord is a validation library written in and for Scala. Its chief aim is to provide a composable, " +
               "dead-simple and self-contained story for defining validation rules and executing them on object " +
               "instances. Feedback, bug reports and improvements are welcome!"

