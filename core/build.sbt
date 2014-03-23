name := "accord-core"

libraryDependencies <++= scalaVersion {
  case v if v startsWith "2.10" =>
    Seq(
      compilerPlugin( "org.scalamacros" % "paradise" % "2.0.0-M7" cross CrossVersion.full ),
      "org.scalamacros" % "quasiquotes" % "2.0.0-M7" cross CrossVersion.full intransitive()
    )
  case _ => Seq.empty
}

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

description := "Accord is a validation library written in and for Scala. Its chief aim is to provide a composable, " +
               "dead-simple and self-contained story for defining validation rules and executing them on object " +
               "instances. Feedback, bug reports and improvements are welcome!"
