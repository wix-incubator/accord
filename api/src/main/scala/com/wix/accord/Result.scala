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

import com.wix.accord.Descriptions.Path

/** A base trait for all violation types. */
sealed trait Violation {
  /** The actual runtime value of the object under validation. */
  def value: Any

  /** A textual description of the constraint being violated (for example, "must not be empty"). */
  def constraint: String

  /** The actual generated path of the object under validation (this is the expression that, when evaluated at
    * runtime, produces the value in [[com.wix.accord.Violation.value]]). This is normally filled in
    * by the validation transform macro, but can also be explicitly provided via the DSL.
    */
  def path: Path

  /** Renders a textual representation of this violation.
    *
    * Important note: This is intended for debugging and logging purposes; there are no guarantees on
    * contents or formatting of the result, and it should not be relied on for production purposes!
    */
  override def toString: String
}

/** Describes a simple validation rule violation (i.e. one without hierarchy). Most built-in combinators
  * emit this type of violation.
  *
  * @param value The value of the object which failed the validation rule.
  * @param constraint A textual description of the constraint being violated (for example, "must not be empty").
  * @param path The path to the object under validation.
  */
case class RuleViolation( value: Any,
                          constraint: String,
                          path: Path = Path.empty )
  extends Violation {

  override def toString: String = {
    val includeValue =
      value match {
        case null | "" => false
        case v: Iterable[_] if v.isEmpty => false
        case _ => true
      }

    if ( includeValue )
      s"""${ Descriptions.render( path ) } with value "$value" $constraint"""
    else
      s"""${ Descriptions.render( path ) } $constraint"""
  }
}

/** Describes the violation of a group of constraints. For example, the `Or` combinator found in the built-in
  * combinator library produces a group violation when all of its predicates fail.
  *
  * @param value The value of the object which failed validation.
  * @param constraint A textual description of the constraint being violated (for example, "doesn't meet any
  *                   of the requirements").
  * @param path The path to the object under validation.
  * @param children The set of violations contained within the group.
  */
case class GroupViolation( value: Any,
                           constraint: String,
                           children: Set[ Violation ],
                           path: Path = Path.empty )
  extends Violation {

  private def renderHeader =
    ( if ( value != null )
        s"""${ Descriptions.render( path ) } with value "$value" $constraint"""
      else
        s"""${ Descriptions.render( path ) } $constraint""" ) +
    ( if ( children.nonEmpty ) ":" else "" )

  private def renderPrefix( nesting: Int, isLast: Boolean ) =
    ( " " * ( if ( nesting > 1 ) 1 else 0 ) ) +
    ( "   " * ( nesting - 1 ) ) +
    ( ( if ( isLast ) '`' else '|' ) + "-- " ) * ( if ( nesting > 0 ) 1 else 0 )

  private def renderSingleChild( nesting: Int, isLast: Boolean )( child: Violation ) =
    child match {
      case rv: RuleViolation => renderPrefix( nesting, isLast ) + rv.toString
      case gv: GroupViolation => gv.render( nesting, isLast )
    }

  private implicit val childOrdering =
    Ordering.fromLessThan[ Violation ] {
      case ( _: RuleViolation, _: GroupViolation ) => true
      case ( _: GroupViolation, _: RuleViolation ) => false
      case ( l, r ) => Descriptions.render( l.path ) < Descriptions.render( r.path )
    }

  private def render( nesting: Int, isLast: Boolean ): String = {
    val sorted = children.toSeq.sorted
    val rendered =
      sorted.dropRight( 1 ).map( renderSingleChild( nesting + 1, isLast = false ) ) ++
      sorted.lastOption.map( renderSingleChild( nesting + 1, isLast = true ) )
    ( ( renderPrefix( nesting, isLast ) + renderHeader ) +: rendered ).mkString( "\n" )
  }

  override def toString: String = render( 0, isLast = true )
}

/** A base trait for validation results.
  *
  * @see [[com.wix.accord.Success]], [[com.wix.accord.Failure]]
  */
sealed trait Result {
  /** Returns `true` if this result represents a successful validation `false` otherwise.  */
  def isSuccess: Boolean

  /** Returns `true` if this result represents a failed validation, `false` otherwise.  */
  def isFailure: Boolean

  /**
    * Returns a new result representing successful validation of both rules, or failure or either.
    *
    * @param other Another result to be composed with this one.
    * @return The resulting instance of [[com.wix.accord.Result]].
    */
  def and( other: Result ): Result

  /**
    * Returns a new result representing successful validation of either rule, or failure or both.
    *
    * @param other Another result to be composed with this one.
    * @return The resulting instance of [[com.wix.accord.Result]].
    */
  def or( other: Result ): Result

  /**
    * Maps over all violations contained in this result (if any).
    *
    * @param f A function that transforms a set of violations.
    * @return The result after the transformation is applied.
    */
  def map( f: Set[ Violation ] => Set[ Violation ] ): Result

  /**
    * Returns a projection of this result as an optional failure.
    *
    * @return [[scala.None None]] if this result represents a success, a wrapped [[com.wix.accord.Failure]] otherwise.
    */
  def toFailure: Option[ Failure ]
}

/** An object representing a successful validation result. */
case object Success extends Result {
  override def and( other: Result ): Result = other
  override def or( other: Result ): Success.type = this
  override def isSuccess: Boolean = true
  override def isFailure: Boolean = false
  override def map( f: Set[ Violation ] => Set[ Violation ] ): Success.type = this
  override def toFailure: Option[ Failure ] = None
}

/** An object representing a failed validation result.
  *
  * @param violations The violations that caused the validation to fail.
  */
case class Failure( violations: Set[ Violation ] ) extends Result {
  override def and( other: Result ): Failure = other match {
    case Success => this
    case Failure( vother ) => Failure( violations ++ vother )
  }

  override def or( other: Result ): Result = other match {
    case Success => other
    case Failure(_) => this
  }

  override def map( f: Set[ Violation ] => Set[ Violation ] ): Failure =
    Failure( f( violations ) )

  override def isSuccess: Boolean = false
  override def isFailure: Boolean = true
  override def toFailure: Option[ Failure ] = Some( this )
}
