import sbt.Project

object Helpers {
  lazy val javaVersion = System.getProperty( "java.vm.specification.version" ).toDouble

  implicit class ConditionalExtensions[ T ]( x: T ) {
    def whenJavaVersion[ R >: T ]( cond: Double => Boolean )( thunk: T => R ): R =
      if ( cond( javaVersion ) ) thunk( x ) else x
  }
}
