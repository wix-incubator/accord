name := "accord-scalatest"

libraryDependencies <+= scalaVersion {
  case v if v startsWith "2.12" => "org.scalatest" %% "scalatest" % "2.2.5-M2"
  case _ => "org.scalatest" %% "scalatest" % "2.2.4"
}

description := "ScalaTest matchers for the Accord validation library"
