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

  /** Describes a "path" through the object graph, from the root to a specific object under validation.
    * The empty path refers to the root object itself (a "self reference").
    */
  type Path = Seq[ Description ]
  object Path {
    def apply( elements: Description* ): Path = elements.toVector
    def empty = Nil
  }

  /** Root trait whose various cases describe a single Object Under Validation (OUV). */
  sealed trait Description

  /** Trait for descriptions that are associated with another element (for example, indexed sequence access). */
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

  /**
    * Denotes that the desirable validation strategy depends of the result of an `if` statement.
    *
    * For example, the following rule:
    * {{{
    *   if (person.age < 18)
    *     person.guardian is notEmpty
    *   else
    *     person.residencyAddress is notEmpty
    * }}}
    *
    * May evaluate to either of the following descriptions:
    * - Branch( guard = Generic( "age < 18" ), evaluation = true )
    * - Branch( guard = Generic( "age < 18" ), evaluation = false )
    *
    * @param guard Describes the specified branch condition.
    * @param evaluation THe result of evaluating the branch condition.
    */
  case class Branch( guard: Generic, evaluation: Boolean ) extends AssociativeDescription

  /** Denotes that the desirable validation strategy depends on the result of a pattern match.
    *
    * For example, the following rule:
    * {{{
    *   person.age match {
    *     case age if age < 18 => person.guardian is notEmpty
    *     case age if age >= 18 => person.residencyAddress is notEmpty
    *   }
    * }}}
    *
    * May evaluate to either of the following descriptions:
    * - PatternMatch( on = AccessChain( "age" ),
    *                 value = -5,
    *                 guard = Some( Generic( "age < 18" ) ) )
    * - PatternMatch( on = AccessChain( "age" ),
    *                 value = 55,
    *                 guard = Some( Generic( "age >= 18" ) ) )
    *
    * @param on Describes the expression being matched on.
    * @param value The value of the expression being matched on.
    * @param guard Describes the guard (`if` clause) of the matching case, if applicable.
    */
  case class PatternMatch( on: Path, value: Any, guard: Option[ Generic ] ) extends AssociativeDescription


  /** Generates a textual representation of the specified description. */
  def render( description: Description ): String =
    description match {
      case Indexed( index ) => s"[at index $index]"
      case Explicit( s ) => s
      case Generic( s ) => s
      case Branch( guard, evaluation ) => s"[where ${render( guard )} is $evaluation]"
      case PatternMatch( on, value, None ) => s"[where ${render( on )}=$value]"
      case PatternMatch( on, value, Some( guard ) ) => s"[where ${render( on )}=$value and ${render( guard )}]"
    }

  /** Generates a textual representation of the specified path. */
  def render( path: Path ): String =
    if ( path.isEmpty )
      "value"
    else {
      val sb = new StringBuilder

      @tailrec def process( tail: Path, emitted: Boolean = true ): Unit =
        tail match {
          case Nil =>

          case ( assoc: AssociativeDescription ) +: remainder =>
            if ( !emitted ) sb append "value"
            sb append ' '
            sb append render( assoc )
            process( remainder )

          case next +: remainder =>
            if ( emitted ) sb append '.'
            sb append render( next )
            process( remainder )
        }

      process( path, emitted = false )
      sb.result()
    }

  // ----------------------------------------------------------------
  // Backwards-compatibility features to simplify migration from 0.6:
  // ----------------------------------------------------------------

  import scala.language.implicitConversions

  @deprecated(
    "Descriptions no longer describe full paths; use com.wix.accord.Descriptions.Path.apply instead",
    since = "0.7" )
  implicit def descriptionToPath( description: Description ): Path =
    Option( description ).map( Path(_) ).orNull   // Option wrangling to support "group( null: Description, ... )"

  object AccessChain {
    @deprecated(
      "AccessChain has been replaced with Path; use com.wix.accord.Descriptions.Path.apply instead",
      since = "0.7" )
    def apply( elements: Description* ): Path = Path( elements:_* )

    @deprecated( "AccessChain has been replaced with Path; match on the path directly instead", since = "0.7" )
    def unapply( path: Path ): Option[ Seq[ Description ] ] = Some( path )
  }

  object Conditional {
    @deprecated(
      "Conditional is deprecated; use one of com.wix.accord.Descriptions.{Branch, PatternMatch} instead",
      since = "0.7" )
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

  @deprecated( "SelfReference is deprecated; use com.wix.accord.Descriptions.Path.empty instead", since = "0.7" )
  val SelfReference: Path = Path.empty
}
