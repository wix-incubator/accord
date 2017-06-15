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

import scala.annotation.tailrec

object Descriptions {

  type Path = Seq[ Description ]
  object Path {
    def apply( elements: Description* ): Path = elements.toVector
    def empty = Nil
  }

  // TODO ---
  // This significantly aids in backwards compatibility. Maybe retain?
  import scala.language.implicitConversions
  implicit def descriptionToPath( description: Description ): Path =
    Option( description ).map( Path(_) ).orNull   // Option wrangling to support "group( null: Description, ... )"

  object AccessChain {
    @deprecated( "Use com.wix.accord.Descriptions.Path.apply instead", since = "0.7" )
    def apply( elements: Description* ): Path = Path( elements:_* )
    @deprecated( "Match on the path directly instead", since = "0.7" )
    def unapply( path: Path ): Option[ Seq[ Description ] ] = Some( path )
  }

  object Conditional {
    @deprecated( "Use one of com.wix.accord.Descriptions.{Branch, PatternMatch} instead", since = "0.7" )
    def apply( on: Path, value: Any, guard: Option[ Generic ] ): AssociativeDescription =
      on match {
        case Generic( "branch" ) :: Nil =>
          val evaluation =
            try value.asInstanceOf[ Boolean ]
            catch {
              case e: ClassCastException => throw new IllegalArgumentException( "Non-boolean branch value specified" )
            }
          Branch(
            guard = guard.getOrElse( throw new IllegalArgumentException( "Cannot create a branch without a guard" ) ),
            evaluation = evaluation
          )

        case _ => PatternMatch( on, value, guard )
      }
  }

  @deprecated( "Use com.wix.accord.Descriptions.Path.empty instead.", since = "0.7" )
  val SelfReference: Path = Path.empty

  // TODO ---

  /** Root trait whose various cases describe a single Object Under Validation (OUV). */
  sealed trait Description

  sealed trait AssociativeDescription extends Description

  /** Denotes an index access (e.g. accessing the nth element of an array). */
  case class Indexed( index: Long ) extends AssociativeDescription

  /** Denotes an explicit textual description, typically provided via the DSL `as` keyword. */
  case class Explicit( description: String ) extends Description

  /**
    * Denotes a generic textual description, typically a piece of code, where a better description could
    * not be generated (for example: the field name as part of an [[com.wix.accord.Descriptions.AccessChain]]).
    */
  case class Generic( description: String ) extends Description

  case class Branch( guard: Generic, evaluation: Boolean ) extends AssociativeDescription

  case class PatternMatch( on: Path, value: Any, guard: Option[ Generic ] ) extends AssociativeDescription

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
    *                guard = Some( Generic( "age < 18" ) ) )
    * - Conditional( on = AccessChain( "age" ),
    *                value = 55,
    *                guard = Some( Generic( "age >= 18" ) ) )
    *
    * @param on A description of the property on which validation branches, or `Generic( "Branch" )` if not applicable.
    * @param value The runtime value of the condition for the matching case.
    * @param guard An optional description of the guard specified for the matching case.
    */
//  case class Conditional( on: Path,
//                          value: Any,
//                          guard: Option[ Generic ] ) extends AssociativeDescription


  // Description algebra --

  def render( description: Description ): String =
    description match {
      case Indexed( index ) => s"[at index $index]"
      case Explicit( s ) => s
      case Generic( s ) => s
      case Branch( guard, evaluation ) => s"[where ${render( guard )} is $evaluation]"
      case PatternMatch( on, value, None ) => s"[where ${render( on )}=$value]"
      case PatternMatch( on, value, Some( guard ) ) => s"[where ${render( on )}=$value and ${render( guard )}]"
    }

  def render( path: Path ): String =
    if ( path.isEmpty )
      "value"
    else {
      val sb = new StringBuilder

      @tailrec def process( tail: Path, emitted: Boolean = true ): Unit =
        tail match {
          case Nil =>

          case ( assoc: AssociativeDescription ) +: Nil =>
            if ( !emitted ) sb append "value"
            sb append ' '
            sb append render( assoc )

          case ( assoc: AssociativeDescription ) +: remainder =>
            if ( !emitted ) sb append "value"
            sb append ' '
            sb append render( assoc )
            process( remainder )

          case ( exp: Explicit ) +: Nil =>
            if ( emitted ) sb append '.'
            sb append render( exp )

          case ( exp: Explicit ) +: next +: remainder =>
            if ( emitted ) sb append '.'
            sb append render( exp )
            process( remainder )

          case next +: remainder =>
            if ( emitted ) sb append '.'
            sb append render( next )
            process( remainder )
        }

      process( path, emitted = false )
      sb.result()
    }
}
