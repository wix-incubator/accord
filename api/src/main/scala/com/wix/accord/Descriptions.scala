/*
  Copyright 2013-2015 Wix.com

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.wix.accord

object Descriptions {
  /** Root trait whose various cases describe a single Object Under Validation (OUV). */
  sealed trait Description
  /**
    * An empty (i.e. unknown) description. This is the default state of any violation prior to applying additional
    * information via [[com.wix.accord.Descriptions.combine]].
    */
  case object Empty extends Description
  /** Denotes an index access (e.g. accessing the nth element of an array). */
  case class Indexed( index: Long, of: Description = Empty ) extends Description
  /** Denotes an explicit textual description, typically provided via the DSL `as` keyword. */
  case class Explicit( description: String ) extends Description

  /**
    * Denotes a generic textual description, typically a piece of code, where a better description could
    * not be generated.
    */
  case class Generic( description: String ) extends Description

  /**
    * Denotes an indirection chain. For example, the expression `field.subfield.subsubfield` would result in
    * a description like `AccessChain( "field", "subfield", "subsubfield" )`.
    */
  case class AccessChain( elements: String* ) extends Description

  /** Denotes a self-reference (i.e. using a single positional wildcard in a lambda). */
  case object SelfReference extends Description

  // Description algebra --

  val combine: ( ( Description, Description ) => Description ) = {
    case ( lhs, Empty ) => lhs
    case ( Empty, rhs ) => rhs

    case ( lhs, Indexed( index, Empty ) ) => Indexed( index, lhs )
    case ( Indexed( index, Empty ), rhs ) => Indexed( index, rhs )

    case ( lhs, SelfReference ) => lhs
    case ( SelfReference, rhs ) => rhs

    case ( AccessChain( left @ _* ), AccessChain( right @ _* ) ) => AccessChain( left ++ right :_* )

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
