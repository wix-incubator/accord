name := "accord-spring3"

libraryDependencies <+= scalaVersion( "org.scala-lang" % "scala-reflect" % _ % "provided" )

libraryDependencies ++= Seq(
    "javax.validation" % "validation-api" % "1.0.0.GA",
    "org.springframework" % "spring-context" % "3.2.5.RELEASE",
    "org.springframework" % "spring-test" % "3.2.5.RELEASE" % "test",
    "org.apache.bval" % "org.apache.bval.bundle" % "0.5" % "test"
)

description := "Spring 3.x Validation integration for the Accord validation library"
