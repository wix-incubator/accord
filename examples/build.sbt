name := "accord-examples"

libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-compiler" % _ % "provided" )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.3" % "test"
)

description := "Sample projects for the Accord validation library."

