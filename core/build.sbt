name := "accord-core"

libraryDependencies <++= scalaVersion {
  case v if v startsWith "2.10" =>
    Seq(
      compilerPlugin( "org.scalamacros" % "paradise" % "2.0.0-M7" cross CrossVersion.full ),
      "org.scalamacros" %% "quasiquotes" % "2.0.0-M7" intransitive()
    )
  case _ => Seq.empty
}

libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-reflect" % _ % "provided" )

libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-compiler" % _ % "provided" )

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "test"

unmanagedSourceDirectories in Compile <+= ( scalaVersion, baseDirectory ) { case ( sv, base ) => sv match {
  case v if v startsWith "2.10" => base / "src/main/scala-2.10"
  case v if v startsWith "2.11" => base / "src/main/scala-2.11"
  case v                        => throw new IllegalStateException( s"Unsupported Scala version $v" )
} }

description := "Accord is a validation library written in and for Scala. Its chief aim is to provide a composable, " +
               "dead-simple and self-contained story for defining validation rules and executing them on object " +
               "instances. Feedback, bug reports and improvements are welcome!"
