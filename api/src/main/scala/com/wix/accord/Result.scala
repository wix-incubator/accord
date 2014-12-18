/*
  Copyright 2013-2014 Wix.com

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

/** A base trait for all violation types. */ 
trait Violation[ +C ] {
  /** The actual runtime value of the object under validation. */
  def value: Any
  /** A textual description of the constraint being violated (for example, "must not be empty"). */
  def constraint: C
  /** The textual description of the object under validation (this is the expression that, when evaluated at
    * runtime, produces the value in [[com.wix.accord.Violation.value]]). This is normally filled in
    * by the validation transform macro, but can also be explicitly provided via the DSL.
    */
  def description: Option[ String ]

  /** Rewrites the description for this violation (used internally by the validation transform macro). As
    * violations are immutable, in practice this returns a modified copy.
    *
    * @param rewrite The rewritten description.
    * @return A modified copy of this violation with the new description in place.
    */
  def withDescription( rewrite: String ): Violation[ C ]
}

/** Describes the violation of a validation rule or constraint.
  * 
  * @param value The value of the object which failed the validation rule.
  * @param constraint A textual description of the constraint being violated (for example, "must not be empty").
  * @param description The textual description of the object under validation.
  */
case class RuleViolation[ C ]( value: Any, constraint: C, description: Option[ String ] ) extends Violation[ C ] {
  def withDescription( rewrite: String ) = this.copy( description = Some( rewrite ) )
}

/** Describes the violation of a group of constraints. For example, the [[com.wix.accord.combinators.Or]]
  * combinator produces a group violation when all predicates fail.
  *
  * @param value The value of the object which failed the validation rule.
  * @param constraint A textual description of the constraint being violated (for example, "must not be empty").
  * @param description The textual description of the object under validation.
  * @param children The set of violations contained within the group.
  */
case class GroupViolation[ C ]( value: Any, constraint: C, description: Option[ String ], children: Set[ Violation[ C ] ] )
  extends Violation[ C ] {
  def withDescription( rewrite: String ) = this.copy( description = Some( rewrite ) )
}

/** A base trait for validation results.
  * @see [[com.wix.accord.Success]], [[com.wix.accord.Failure]]
  */
sealed trait Result[ +C ] {
  def and[ C2 >: C ]( other: Result[ C2 ] ): Result[ C2 ]
  def or[ C2 >: C ]( other: Result[ C2 ] ): Result[ C2 ]

  /** Rewrites the description for all violations, if applicable.
    *
    * @param rewrite The rewritten description.
    * @return A modified copy of this result with the new description in place.
    */
  def withDescription( rewrite: String ): Result[ C ]
}

/** An object representing a successful validation result. */
case object Success extends Result[ Nothing ] {
  def and[ C2 ]( other: Result[ C2 ] ): Result[ C2 ] = other
  def or[ C2 ]( other: Result[ C2 ] ): Result[ C2 ] = this
  def withDescription( rewrite: String ) = this
}

/** An object representing a failed validation result.
  * @param violations The violations that caused the validation to fail.
  */
case class Failure[ C ]( violations: Set[ Violation[ C ] ] ) extends Result[ C ] {
  def and[ C2 >: C ]( other: Result[ C2 ] ): Result[ C2 ] =
    other match {
      case Success => this
      case Failure( vother ) =>
        // Not entirely sure why the ascription on the next line is needed, but the typer spits out
        // Set[Violation[Any]] otherwise...
        val s = ( ( vother: Iterable[ Violation[ C2 ] ] ) ++ violations.toIterable ).toSet
        Failure( s )
    }

  def or[ C2 >: C ]( other: Result[ C2 ] ): Result[ C2 ] =
    other match {
      case Success => other
      case Failure(_) => this
    }

  def withDescription( rewrite: String ): Result[ C ] =
    Failure( violations map { _ withDescription rewrite } )
}
