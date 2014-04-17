name := "accord-specs2"

libraryDependencies <+= scalaVersion( CrossVersion.partialVersion(_) match {
  case Some(( 2, 11 )) => "org.specs2" % "specs2_2.11.0-RC4" % "2.3.10"     // Temporary hack 'till official Specs2 comes out
  case _               => "org.specs2" %% "specs2" % "2.3.10"
} )

description := "Specs² matchers for the Accord validation library"
