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

trait Results {
  self: Constraints =>

  /** The default failure for null validations. */
  val nullFailure: Failure = Failure( Set( RuleViolation( null, isNullConstraint, None ) ) )

  /** A base trait for all violation types. */
  trait Violation {
    /** The actual runtime value of the object under validation. */
    def value: Any
    /** A textual description of the constraint being violated (for example, "must not be empty"). */
    def constraint: Constraint
    /** The textual description of the object under validation (this is the expression that, when evaluated at
      * runtime, produces the value in [[com.wix.accord.Results#Results#Violation.value]]). This is normally filled in
      * by the validation transform macro, but can also be explicitly provided via the DSL.
      */
    def description: Option[ String ]

    /** Rewrites the description for this violation (used internally by the validation transform macro). As
      * violations are immutable, in practice this returns a modified copy.
      *
      * @param rewrite The rewritten description.
      * @return A modified copy of this violation with the new description in place.
      */
    def withDescription( rewrite: String ): Violation
  }

  /** Describes the violation of a validation rule or constraint.
    *
    * @param value The value of the object which failed the validation rule.
    * @param constraint A textual description of the constraint being violated (for example, "must not be empty").
    * @param description The textual description of the object under validation.
    */
  case class RuleViolation( value: Any, constraint: Constraint, description: Option[ String ] ) extends Violation {
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
  case class GroupViolation( value: Any, constraint: Constraint, description: Option[ String ], children: Set[ Violation ] )
    extends Violation {
    def withDescription( rewrite: String ) = this.copy( description = Some( rewrite ) )
  }

  /** A base trait for validation results.
    * @see [[com.wix.accord.Results#Results#Success]], [[com.wix.accord.Results#Results#Failure]]
    */
  sealed trait Result {
    def and( other: Result ): Result
    def or( other: Result ): Result

    def fold[ T ]( ifSuccess: => T )( ifFailure: Failure => T ): T
    def success: Option[ Success.type ] =
      fold( Option( Success ) )( _ => None )
    def failure: Option[ Failure ] =
      fold[ Option[ Failure ] ]( None )( Option.apply )
    def isSuccess: Boolean = fold( true )( _ => false )
    def isFailure: Boolean = fold( false )( _ => true )
    def ifSuccess[ T ]( pred: => T ): Option[ T ] =
      fold( Option( pred ) )( _ => None )
    def ifFailure[ T ]( pred: Failure => T ): Option[ T ] =
      fold[ Option[ T ] ]( None )( pred andThen Option.apply )

    /** Rewrites the description for all violations, if applicable.
      *
      * @param rewrite The rewritten description.
      * @return A modified copy of this result with the new description in place.
      */
    def withDescription( rewrite: String ): Result
  }

  /** An object representing a successful validation result. */
  sealed abstract class Success extends Result {
    def and( other: Result ) = other
    def or( other: Result ) = this
    def withDescription( rewrite: String ) = this
    def fold[ T ]( ifSuccess: => T )( ifFailure: Failure => T ): T = ifSuccess
  }
  case object Success extends Success

  /** An object representing a failed validation result.
    * @param violations The violations that caused the validation to fail.
    */
  case class Failure( violations: Set[ Violation ] ) extends Result {
    def and( other: Result ) = other match {
      case _: Success => this
      case Failure( vother ) => Failure( violations ++ vother )
    }
    def or( other: Result ) = other match {
      case _: Success => other
      case Failure(_) => this
    }
    def withDescription( rewrite: String ) = Failure( violations map { _ withDescription rewrite } )
    def fold[ T ]( ifSuccess: => T )( ifFailure: Failure => T ): T = ifFailure( this )
  }
}
