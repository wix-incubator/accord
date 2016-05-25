package com.wix.accord

/**
  * Created by tomerga on 05/05/2016.
  */
object Descriptions {
  sealed trait Description
  case object Empty extends Description
  case class Indexed( index: Long, of: Description = Empty ) extends Description
  case class Explicit( description: String ) extends Description
  case class Generic( description: String ) extends Description
  case class AccessChain( elements: String* ) extends Description
  case object SelfReference extends Description

  // Description algebra --

  def combine( lhs: Description, rhs: Description ): Description =
    ( lhs, rhs ) match {
      case ( _, Empty ) => lhs
      case ( Empty, _ ) => rhs

      case ( _, Indexed( index, Empty ) ) => Indexed( index, lhs )
      case ( Indexed( index, Empty ), _ ) => Indexed( index, rhs )

      case _ => throw new IllegalArgumentException( s"Cannot combine description '$lhs' with '$rhs'" )
    }

  def render( description: Description ): String = description match {
    case Empty => "unknown"
    case Indexed( index, of ) => s"${render( of )} [at index $index]"
    case Explicit( s ) => s
    case Generic( s ) => s
    case AccessChain( elements ) => elements.mkString( "." )
    case SelfReference => "value"
  }
}
