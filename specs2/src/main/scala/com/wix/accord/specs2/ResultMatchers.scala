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

package com.wix.accord.specs2

import com.wix.accord.Descriptions.Description

import scala.language.implicitConversions
import com.wix.accord._
import org.specs2.matcher.{Expectable, Matcher}

/** Extends a specification with a set of matchers over validation [[com.wix.accord.Result]]s. */
trait ResultMatchers {

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
                                   constraint: String = null,
                                   legacyDescription: String = null,
                                   description: Description = null )
    extends ViolationMatcher {

    require( value != null || constraint != null || legacyDescription != null || description != null )

    def apply[ T <: Violation ]( left: Expectable[ T ] ) = left.value match {
      case rv: RuleViolation =>
        result(
          test = ( value             == null || value             == rv.value                              ) &&
                 ( constraint        == null || constraint        == rv.constraint                         ) &&
                 ( legacyDescription == null || legacyDescription == Descriptions.render( rv.description ) ) &&
                 ( description       == null || description       == rv.description                        ),
          s"Rule violation $rv matches pattern $this",
          s"Rule violation $rv did not match pattern $this",
          left
        )
      case _ =>
        result( test = false,
          s"${left.description} is a rule violation",
          s"${left.description} is not a rule violation",
          left )
    }

    override def toString() =
      Seq( Option( value ) getOrElse "_",
           Option( constraint ) getOrElse "_",
           Option( description ) orElse Option( legacyDescription ) getOrElse "_" )
      .mkString( "RuleViolation(", ",", ")" )
  }

  /** A convenience implicit to simplify test code. Enables syntax like:
    *
    * ```
    * val rule: RuleViolationMatcher = "firstName" -> "must not be empty"
    * // ... which is equivalent to
    * val rule = RuleViolationMatcher( legacyDescription = "firstName", constraint = "must not be empty" )
    * ```
    */
  @deprecated( "Intended for backwards compatibility. It is recommended to match against Description types instead " +
               "(see descriptionAndConstraintTuple2RuleMatcher).",
               since = "0.6" )
  implicit def stringTuple2RuleMatcher( v: ( String, String ) ): RuleViolationMatcher =
    RuleViolationMatcher( legacyDescription = v._1, constraint = v._2 )

  /** A convenience implicit to simplify test code. Enables syntax like:
    *
    * ```
    * val rule: RuleViolationMatcher = AccessChain( "firstName" ) -> "must not be empty"
    * // ... which is equivalent to
    * val rule = RuleViolationMatcher( description = AccessChain( "firstName" ), constraint = "must not be empty" )
    * ```
    */
  implicit def descriptionAndConstraintTuple2RuleMatcher( v: ( Description, String ) ): RuleViolationMatcher =
    RuleViolationMatcher( description = v._1, constraint = v._2 )

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
                                    constraint: String = null,
                                    legacyDescription: String = null,
                                    description: Description = null,
                                    violations: Set[ ViolationMatcher ] = null )
    extends ViolationMatcher {

    require( value       != null || constraint != null || legacyDescription != null ||
      description != null || violations != null )

    def apply[ T <: Violation ]( left: Expectable[ T ] ) = left.value match {
      case gv: GroupViolation =>
        val rulesMatch = violations == null ||
                         ( gv.children.size == violations.size &&
                           gv.children.forall( rule => violations.exists( _ test rule ) ) )
        result(
          test = ( value       == null || gv.value       == value       ) &&
                 ( constraint        == null || constraint        == gv.constraint                         ) &&
                 ( legacyDescription == null || legacyDescription == Descriptions.render( gv.description ) ) &&
                 ( description       == null || description       == gv.description                        ) &&
                 rulesMatch,
          s"Group violation $gv matches pattern $this",
          s"Group violation $gv does not match pattern $this",
          left
        )
      case _ =>
        result( test = false,
          s"${left.description} is a group violation",
          s"${left.description} is not a group violation",
          left )
    }

    override def toString() =
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
    def apply[ T <: Result ]( left: Expectable[ T ] ) = left.value match {
      case Success =>
        result( test = false, "Validation was not successful", "Validation was successful", left )

      case Failure( violations ) =>
        val matched = violations.map { v => ( v, expectedViolations.find( _ test v ) ) }
        val unexpected = matched collect { case ( v, None ) => v }
        val unmatched = expectedViolations.diff( matched collect { case ( _, Some( rule ) ) => rule } )

        result( test = unexpected.isEmpty && unmatched.isEmpty,
          s"Validation of ${left.description} successful",
          s"""
             |Validation of ${left.description} failed!
             |Unexpected violations:
             |${unexpected.mkString( "\t", "\n\t", "" )}
             |Expected violations that weren't found:
             |${unmatched.mkString( "\t", "\n\t", "" )}
           """.stripMargin,
          left
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
  def group( description: Description, constraint: String, expectedViolations: ( Description, String )* ) =
    GroupViolationMatcher( constraint  = constraint,
      description = description,
      violations  = ( expectedViolations map descriptionAndConstraintTuple2RuleMatcher ).toSet )

  /** A convenience method for matching violation groups. Enables syntax like:
    *
    * ```
    * val result: Result = ...
    * result should failWith( group( AccessChain( "teacher" ), "is invalid",                // The group context
    *                                AccessChain( "firstName ) -> "must not be empty" ) )   // The rule violations
    * ```
    *
    * @param constraint A textual description of the constraint being violated (for example, "must not be empty").
    * @param legacyDescription Retained for backwards compatibility; matches against the rendered description of
    *                          the object being validated. See [[com.wix.accord.Descriptions.render]] for details
    *                          of how descriptions are rendered into strings.
    * @param expectedViolations The set of expected violations that comprise the group.
    * @return A matcher over [[com.wix.accord.GroupViolation]]s.
    */
  //noinspection ScalaDeprecation
  @deprecated( "Intended for backwards compatibility. It is recommended to match against Description types instead.",
               since = "0.6" )
  def group( legacyDescription: String, constraint: String, expectedViolations: ( String, String )* ) =
  GroupViolationMatcher( constraint  = constraint,
    legacyDescription = legacyDescription,
    violations  = ( expectedViolations map stringTuple2RuleMatcher ).toSet )

  // TODO docs
  @deprecated( "Intended for backwards compatibility. It is recommended to match against Description types instead.",
               since = "0.6" )
  def group[ T ]( legacyDescription: String, constraint: String, expectedViolations: T* )
                ( implicit ev: T => RuleViolationMatcher ): GroupViolationMatcher =
  GroupViolationMatcher( constraint        = constraint,
    legacyDescription = legacyDescription,
    violations        = ( expectedViolations map ev ).toSet )

  def group[ T ]( description: Description, constraint: String, expectedViolations: T* )
                ( implicit ev: T => RuleViolationMatcher ): GroupViolationMatcher =
    GroupViolationMatcher( constraint  = constraint,
      description = description,
      violations  = ( expectedViolations map ev ).toSet )

  /** Enables syntax like `someResult should fail` */
  val fail = new Matcher[ Result ] {
    def apply[ T <: Result ]( left: Expectable[ T ] ) =
      result( test = left.value.isInstanceOf[ Failure ],
        s"${left.description} is a failure",
        s"${left.description} is not a failure",
        left )
  }

  /** Enables syntax like `someResult should succeed` */
  val succeed = new Matcher[ Result ] {
    def apply[ T <: Result ]( left: Expectable[ T ] ) = {
      val ( success, violations ) = left.value match {
        case Success => ( true, Seq.empty )
        case f: Failure => ( false, f.violations )
      }
      result( test = success,
        s"${left.description} is a success",
        s"${left.description} is not a success (violations: ${violations.mkString( ", " )})",
        left )
    }
  }
}
