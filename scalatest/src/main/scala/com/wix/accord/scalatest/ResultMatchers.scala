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

package com.wix.accord.scalatest

import com.wix.accord.Descriptions.Description

import scala.language.implicitConversions
import org.scalatest.Suite
import org.scalatest.matchers.{BeMatcher, MatchResult, Matcher}
import com.wix.accord._

/** Extends a test suite with a set of matchers over validation [[com.wix.accord.Result]]s. */
trait ResultMatchers {
  self: Suite =>

  /** Abstracts over validators for the various violation type. */
  sealed trait ViolationMatcher extends Matcher[ Violation ]

  /** A matcher over [[com.wix.accord.RuleViolation]]s. To generate a violation rule "pattern", call
    * the constructor with the required predicates, for example:
    *
    * ``` 
    * val firstNameNotEmpty =
    *   RuleViolationMatcher( description = AccessChain( "firstName" ), constraint = "must not be empty" )
    * val validationResult: Result = ...
    * validationResult must failWith( firstNameNotEmpty )
    * ```
    * 
    * @param value A predicate specifying the object under validation.
    * @param constraint A predicate specifying the constraint being violated.
    * @param description A predicate specifying the description of the object being validated.
    * @param legacyDescription Retained for backwards compatibility; matches against the rendered description of
    *                          the object being validated. See [[com.wix.accord.Descriptions.render]] for details
    *                          of how descriptions are rendered into strings.
    * @see [[com.wix.accord.RuleViolation]]
    */
  case class RuleViolationMatcher( value: Any = null,
                                   constraint: Any = null,
                                   legacyDescription: String = null,
                                   description: Description = null )
    extends ViolationMatcher {

    require( value != null || constraint != null || legacyDescription != null || description != null )

    def apply( left: Violation ): MatchResult = left match {
      case rv: RuleViolation =>
        MatchResult(
          matches = ( value             == null || value             == rv.value                              ) &&
                    ( constraint        == null || constraint        == rv.constraint                         ) &&
                    ( legacyDescription == null || legacyDescription == Descriptions.render( rv.description ) ) &&
                    ( description       == null || description       == rv.description                        ),
          s"Rule violation $rv did not match pattern $this",
          s"Rule violation $rv matches pattern $this"
        )
      case _ =>
        MatchResult( matches = false,
          s"$left is not a rule violation",
          s"$left is a rule violation" )
    }

    override def toString(): String =
      Seq( Option( value ) getOrElse "_",
           Option( constraint ) getOrElse "_",
           Option( description ) orElse Option( legacyDescription ) getOrElse "_" )
      .mkString( "RuleViolation(", ",", ")" )
  }

  /** A convenience implicit to simplify test code. Enables syntax like:
    *
    * ```
    * val rule: RuleViolationMatcher = AccessChain( "firstName" ) -> "must not be empty"
    * // ... which is equivalent to
    * val rule = RuleViolationMatcher( description = AccessChain( "firstName" ), constraint = "must not be empty" )
    * ```
    */
  implicit def descriptionAndConstraintTuple2RuleMatcher( v: ( Description, Any ) ): RuleViolationMatcher =
    RuleViolationMatcher( description = v._1, constraint = v._2 )

  // TODO ScalaDocs
  // TODO maybe ViolationMatcher? This applies to groups as well
  implicit def description2RuleViolationMatcher( desc: Description ): RuleViolationMatcher =
    RuleViolationMatcher( description = desc )

  /** A matcher over [[com.wix.accord.GroupViolation]]s. To generate a violation rule "pattern", call
    * the constructor with the required predicates, for example:
    *
    * ```
    * val firstNameNotEmpty =
    *   RuleViolationMatcher( description = AccessChain( "firstName" ), constraint = "must not be empty" )
    * val lastNameNotEmpty =
    *   RuleViolationMatcher( description = AccessChain( "lastName" ), constraint = "must not be empty" )
    * val orPredicateFailed =
    *   GroupViolationMatcher( constraint = "doesn't meet any of the requirements",
    *                          violations = firstNameNotEmpty :: lastNameNotEmpty :: Nil )
    * val validationResult: Result = ...
    * validationResult must failWith( orPredicateFailed )
    * ```
    *
    * @param value A predicate specifying the object under validation.
    * @param constraint A predicate specifying the constraint being violated.
    * @param description A predicate specifying the description of the object being validated.
    * @param legacyDescription Retained for backwards compatibility; matches against the rendered description of
    *                          the object being validated. See [[com.wix.accord.Descriptions.render]] for details
    *                          of how descriptions are rendered into strings.
    * @param violations The set of violations that comprise the group being validated.
    * @see [[com.wix.accord.GroupViolation]]
    */
  case class GroupViolationMatcher( value: Any = null,
                                    constraint: Any = null,
                                    legacyDescription: String = null,
                                    description: Description = null,
                                    violations: Set[ ViolationMatcher ] = null )
    extends ViolationMatcher {

    require( value       != null || constraint != null || legacyDescription != null ||
             description != null || violations != null )

    def apply( left: Violation ): MatchResult = left match {
      case gv: GroupViolation =>
        val rulesMatch = violations == null ||
                         ( gv.children.size == violations.size &&
                           gv.children.forall( rule => violations.exists( _.apply( rule ).matches ) ) )
        MatchResult(
          matches = ( value             == null || value             == gv.value                              ) &&
                    ( constraint        == null || constraint        == gv.constraint                         ) &&
                    ( legacyDescription == null || legacyDescription == Descriptions.render( gv.description ) ) &&
                    ( description       == null || description       == gv.description                        ) &&
                    rulesMatch,
          s"Group violation $gv did not match pattern $this",
          s"Group violation $gv matches pattern $this"
        )
      case _ =>
        MatchResult( matches = false,
          s"$left is not a group violation",
          s"$left is a group violation" )
    }

    override def toString(): String =
      Seq( Option( value ) getOrElse "_",
           Option( constraint ) getOrElse "_",
           Option( description ) orElse Option( legacyDescription ) getOrElse "_",
           Option( violations ) getOrElse "_" )
      .mkString( "GroupViolation(", ",", ")" )
  }

  /** A matcher over validation [[com.wix.accord.Result]]s. Takes a set of expected violations
    * and return a suitable match result in case of failure.
    *
    * @param expectedViolations The set of expected violations for this matcher.
    */
  case class ResultMatcher( expectedViolations: Set[ ViolationMatcher ] ) extends Matcher[ Result ] {
    def apply( left: Result ): MatchResult = left match {
      case Success =>
        MatchResult( matches = false, "Validation was successful", "Validation was not successful" )

      case Failure( violations ) =>
        val matched = violations.map { v => ( v, expectedViolations.find( _.apply( v ).matches ) ) }
        val unexpected = matched collect { case ( v, None ) => v }
        val unmatched = expectedViolations.diff( matched collect { case ( _, Some( rule ) ) => rule } )

        MatchResult( matches = unexpected.isEmpty && unmatched.isEmpty,
          s"""
             |Validation of $left failed!
             |Unexpected violations:
             |${unexpected.mkString( "\t", "\n\t", "" )}
             |Expected violations that weren't found:
             |${unmatched.mkString( "\t", "\n\t", "" )}
           """.stripMargin,
          s"Validation of $left successful"
        )
    }
  }

  /** A convenience method for matching failures. Enables syntax like:
    *
    * ```
    * val result: Result = ...
    * result should failWith(
    *   AccessChain( "firstName" ) -> "must not be empty",
    *   AccessChain( "lastName" ) -> "must not be empty"
    * )
    * ```
    *
    * @param expectedViolations The set of expected violations.
    * @return A matcher over validation [[com.wix.accord.Result]]s.
    */
  def failWith( expectedViolations: ViolationMatcher* ): Matcher[ Result ] = ResultMatcher( expectedViolations.toSet )

  /** A convenience method for matching violation groups. Enables syntax like:
    *
    * ```
    * val result: Result = ...
    * result should failWith( group( AccessChain( "teacher" ), "is invalid",                // The group context
    *                                AccessChain( "firstName ) -> "must not be empty" ) )   // The rule violations
    * ```
    *
    * @param constraint A textual description of the constraint being violated (for example, "must not be empty").
    * @param description A predicate specifying the description of the object being validated.
    * @param expectedViolations The set of expected violations that comprise the group.
    * @return A matcher over [[com.wix.accord.GroupViolation]]s.
    */
  def group[ T ]( description: Description, constraint: String, expectedViolations: T* )
                ( implicit ev: T => RuleViolationMatcher ): GroupViolationMatcher =
    GroupViolationMatcher( constraint  = constraint,
                           description = description,
                           violations  = ( expectedViolations map ev ).toSet )

  // TODO check if necessary and remove entirely of not
  //  def group( description: Description, constraint: String, expectedViolations: ( Description, Any )* ) =
  //    GroupViolationMatcher( constraint  = constraint,
  //                           description = description,
  //                           violations  = ( expectedViolations map descriptionAndConstraintTuple2RuleMatcher ).toSet )

  /** Enables syntax like `someResult should be( aFailure )` */
  val aFailure = new BeMatcher[ Result ] {
    def apply( left: Result ) =
      MatchResult( left.isInstanceOf[ Failure ],
        s"$left is not a failure",
        s"$left is a failure"
      )
  }

  /** Enables syntax like `someResult should be( aSuccess )` */
  val aSuccess = new BeMatcher[ Result ] {
    def apply( left: Result ): MatchResult = {
      val ( success, violations ) = left match {
        case Success => ( true, Seq.empty )
        case f: Failure => ( false, f.violations )
      }
      MatchResult( success,
        s"$left is not a success (violations: ${violations.mkString( ", " )})",
        s"$left is a success" )
    }
  }
}
