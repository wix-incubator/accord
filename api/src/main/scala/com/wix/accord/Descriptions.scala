/*
  Copyright 2013-2016 Wix.com

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
    * not be generated (for example: the field name as part of an [[com.wix.accord.Descriptions.AccessChain]]).
    */
  case class Generic( description: String ) extends Description

  /**
    * Denotes an indirection chain. For example, the expression `field.subfield.subsubfield` would result in
    * a description like `AccessChain( Generic( "field" ), Generic( "subfield" ), Generic( "subsubfield" ) )`.
    */
  case class AccessChain( elements: Description* ) extends Description

  /**
    * Denotes that the desirable validation strategy depends on a runtime condition. For example, the following
    * expression:
    *
    * {{{
    *   person.age match {
    *     case age if age < 18 => person.guardian is notEmpty
    *     case age if age >= 18 => person.residencyAddress is notEmpty
    *   }
    * }}}
    *
    * May evaluate to either of the following descriptions:
    * - Conditional( on = AccessChain( "age" ),
    *                value = -5,
    *                guard = Some( Generic( "age < 18" ) ),
    *                target = AccessChain( "guardian" ) )
    * - Conditional( on = AccessChain( "age" ),
    *                value = 55,
    *                guard = Some( Generic( "age >= 18" ) ),
    *                target = AccessChain( "residencyAddress" ) )
    *
    * @param on A description of the property on which validation branches, or `Generic( "Branch" )` if not applicable.
    * @param value The runtime value of the condition for the matching case.
    * @param guard An optional description of the guard specified for the matching case.
    * @param target The description of the validation target for the matching case.
    */
  case class Conditional( on: Description,
                          value: Any,
                          guard: Option[ Description ],
                          target: Description ) extends Description

  /** Denotes a self-reference (i.e. using a single positional wildcard in a lambda). */
  case object SelfReference extends Description

  // Description algebra --

  val combine: ( ( Description, Description ) => Description ) = {
    case ( Empty, rhs ) => rhs
    case ( SelfReference, rhs: AccessChain ) => rhs
    case ( Indexed( index, Empty ), rhs ) => Indexed( index, rhs )
    case ( lhs: Explicit, AccessChain( ind @ _* ) ) => AccessChain( ind :+ lhs :_* )
    case ( lhs: Generic, AccessChain( ind @ _* ) ) => AccessChain( ind :+ lhs :_* )
    case ( lhs @ Indexed( _, of ), AccessChain( ind @ _* ) ) if of != Empty => AccessChain( ind :+ lhs :_* )
    case ( AccessChain( rhs @ _* ), ind @ Indexed( _, Empty ) ) => AccessChain( ind +: rhs : _* )
    case ( AccessChain( ind @ Indexed( _, Empty ), tail @ _* ), rhs ) => AccessChain( ind.copy( of = rhs ) +: tail :_* )
    case ( AccessChain( inner @ _* ), AccessChain( outer @ _* ) ) => AccessChain( outer ++ inner :_* )

    case ( lhs, rhs ) =>
      throw new IllegalArgumentException( s"Cannot combine description '$lhs' with '$rhs'" )
  }

  val render: Description => String = {
    case Empty => "unknown"
    case Indexed( index, of ) => s"${render( of )} [at index $index]"
    case Explicit( s ) => s
    case Generic( s ) => s
    case AccessChain( elements @ _* ) => elements.map( render ).mkString( "." )
    case SelfReference => "value"
    case Conditional( on, value, None, target ) => s"${render( target )} [where ${render( on )}=$value]"
    case Conditional( on, value, Some( guard ), target ) =>
      s"${render( target )} [where ${render( on )}=$value and ${render( guard )}]"
  }
}
