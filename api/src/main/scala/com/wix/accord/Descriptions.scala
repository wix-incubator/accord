package com.wix.accord

import com.sun.org.glassfish.gmbal.Description

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

  val combine: ( ( Description, Description ) => Description ) = {
    case ( lhs, Empty ) => lhs
    case ( Empty, rhs ) => rhs

    case ( lhs, Indexed( index, Empty ) ) => Indexed( index, lhs )
    case ( Indexed( index, Empty ), rhs ) => Indexed( index, rhs )

    case ( lhs, SelfReference ) => lhs
    case ( SelfReference, rhs ) => rhs

    case ( AccessChain( left @ _* ), AccessChain( right @ _* ) ) => AccessChain( ( left ++ right ) :_* )

    case ( lhs, rhs ) =>
      throw new IllegalArgumentException( s"Cannot combine description '$lhs' with '$rhs'" )
  }

  val render: Description => String = {
    case Empty => "unknown"
    case Indexed( index, of ) => s"${render( of )} [at index $index]"
    case Explicit( s ) => s
    case Generic( s ) => s
    case AccessChain( elements @ _* ) => elements.mkString( "." )
    case SelfReference => "value"
  }
}
