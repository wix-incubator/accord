resolvers <++= scalaVersion {
  case v if v startsWith "2.12" =>
    Seq.empty

  case _ =>
    // No support for 0.12 yet, see https://github.com/scoverage/sbt-scoverage/issues/126#issuecomment-124034978
    addSbtPlugin( "org.scoverage" % "sbt-scoverage" % "1.0.4" )
    addSbtPlugin( "org.scoverage" % "sbt-coveralls" % "1.0.0" )
    Seq( Classpaths.sbtPluginReleases )
}
