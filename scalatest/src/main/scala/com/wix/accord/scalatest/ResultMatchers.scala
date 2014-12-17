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

package com.wix.accord.scalatest

import scala.language.implicitConversions
import org.scalatest.Suite
import org.scalatest.matchers.{BeMatcher, MatchResult, Matcher}
import com.wix.accord._

/** Extends a test suite with a set of matchers over validation [[com.wix.accord.Results#Result]]s. */
trait ResultMatchers[ Domain <: Constraints with Results { type Constraint >: Null <: AnyRef } ] {
  self: Suite =>

  /** Abstracts over validators for the various violation type. */
  sealed trait ViolationMatcher extends Matcher[ Domain#Violation ]

  /** A matcher over [[com.wix.accord.Results#Results#RuleViolation]]s. To generate a violation rule "pattern", call
    * the constructor with the required predicates, for example:
    *
    * ``` 
    * val firstNameNotEmpty = RuleViolationMatcher( description = "firstName", constraint = "must not be empty" )
    * val validationResult: Result = ...
    * validationResult must failWith( firstNameNotEmpty )
    * ```
    * 
    * @param value A predicate specifying the object under validation.
    * @param constraint A predicate specifying the constraint being violated.
    * @param description A predicate specifying the description of the object being validated.
    * @see [[com.wix.accord.Results#Results#RuleViolation]]
    */  
  case class RuleViolationMatcher( value: Any = null, constraint: Domain#Constraint = null, description: String = null )
    extends ViolationMatcher {

    require( value != null || constraint != null || description != null )

    def apply( left: Domain#Violation ): MatchResult = left match {
      case rv: Domain#RuleViolation =>
        MatchResult(
          matches = ( value       == null || rv.value       == value               ) &&
                    ( constraint  == null || rv.constraint  == constraint          ) &&
                    ( description == null || rv.description == Some( description ) ),
          s"Rule violation $rv did not match pattern $this",
          s"Rule violation $rv matches pattern $this"
        )
      case _ =>
        MatchResult( matches = false,
          s"$left is not a rule violation",
          s"$left is a rule violation" )
    }

    override def toString() = Seq( Option( value       ) getOrElse "_",
                                   Option( constraint  ) getOrElse "_",
                                   Option( description ) getOrElse "_" ).mkString( "RuleViolation(", ",", ")" )

  }

  /** A convenience implicit to simplify test code. Enables syntax like:
    * 
    * ```
    * val rule: RuleViolationMatcher = "firstName" -> "must not be empty"
    * // ... which is equivalent to
    * val rule = RuleViolationMatcher( description = "firstName", constraint = "must not be empty" )
    * ```
    */
  implicit def tupleToRuleMatcher( v: ( String, Domain#Constraint ) ): RuleViolationMatcher =
    RuleViolationMatcher( description = v._1, constraint = v._2 )

  /** A matcher over [[com.wix.accord.Results#GroupViolation]]s. To generate a violation rule "pattern", call
    * the constructor with the required predicates, for example:
    *
    * ```
    * val firstNameNotEmpty = RuleViolationMatcher( description = "firstName", constraint = "must not be empty" )
    * val lastNameNotEmpty = RuleViolationMatcher( description = "lastName", constraint = "must not be empty" )
    * val orPredicateFailed = GroupViolationMatcher( constraint = "doesn't meet any of the requirements",
    *                                                violations = firstNameNotEmpty :: lastNameNotEmpty :: Nil )
    * val validationResult: Result = ...
    * validationResult must failWith( orPredicateFailed )
    * ```
    *
    * @param value A predicate specifying the object under validation.
    * @param constraint A predicate specifying the constraint being violated.
    * @param description A predicate specifying the description of the object being validated.
    * @param violations The set of violations that comprise the group being validated.
    * @see [[com.wix.accord.Results#GroupViolation]]
    */
  case class GroupViolationMatcher( value: Any = null, constraint: Domain#Constraint = null, description: String = null,
                                    violations: Set[ ViolationMatcher ] = null )
    extends ViolationMatcher {

    require( value != null || constraint != null || description != null || violations != null )

    def apply( left: Domain#Violation ): MatchResult = left match {
      case gv: Domain#GroupViolation =>
        val rulesMatch = violations == null ||
                         ( gv.children.size == violations.size &&
                           gv.children.forall( rule => violations.exists( _.apply( rule ).matches ) ) )
        MatchResult(
          matches = ( value       == null || gv.value       == value               ) &&
                    ( constraint  == null || gv.constraint  == constraint          ) &&
                    ( description == null || gv.description == Some( description ) ) &&
                    rulesMatch,
          s"Group violation $gv did not match pattern $this",
          s"Group violation $gv matches pattern $this"
        )
      case _ =>
        MatchResult( matches = false,
          s"$left is not a group violation",
          s"$left is a group violation" )
    }

    override def toString() = Seq( Option( value       ) getOrElse "_",
                                   Option( constraint  ) getOrElse "_",
                                   Option( description ) getOrElse "_",
                                   Option( violations  ) getOrElse "_" ).mkString( "GroupViolation(", ",", ")" )
  }

  /** A matcher over validation [[com.wix.accord.Results#Result]]s. Takes a set of expected violations
    * and return a suitable match result in case of failure.
    *
    * @param expectedViolations The set of expected violations for this matcher.
    */
  case class ResultMatcher( expectedViolations: Set[ ViolationMatcher ] ) extends Matcher[ Domain#Result ] {
    def apply( left: Domain#Result ) =
      left.fold(
        ifSuccess = MatchResult( matches = false, "Validation was successful", "Validation was not successful" ) )(
        ifFailure = { failure =>
          import scala.language.existentials
          val matched = failure.violations.map { v => v -> expectedViolations.find( _.apply( v ).matches ) }
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
      )


//      left match {
//      case _: Domain#Success =>
//        MatchResult( matches = false, "Validation was successful", "Validation was not successful" )
//
//      case failure: Domain#Failure =>
//        val matched = failure.violations.map { v => ( v, expectedViolations.find( _.apply( v ).matches ) ) }
//        val unexpected = matched collect { case ( v, None ) => v }
//        val unmatched = expectedViolations.diff( matched collect { case ( _, Some( rule ) ) => rule } )
//
//        MatchResult( matches = unexpected.isEmpty && unmatched.isEmpty,
//          s"""
//             |Validation of $left failed!
//             |Unexpected violations:
//             |${unexpected.mkString( "\t", "\n\t", "" )}
//             |Expected violations that weren't found:
//             |${unmatched.mkString( "\t", "\n\t", "" )}
//           """.stripMargin,
//          s"Validation of $left successful"
//        )
//    }
  }

  /** A convenience method for matching failures. Enables syntax like:
    * 
    * ```
    * val result: Result = ...
    * result should failWith( "firstName" -> "must not be empty", "lastName" -> "must not be empty" )
    * ```
    * 
    * @param expectedViolations The set of expected violations.
    * @return A matcher over validation [[com.wix.accord.Results#Result]]s.
    */
  def failWith( expectedViolations: ViolationMatcher* ): Matcher[ Domain#Result ] =
    ResultMatcher( expectedViolations.toSet )

  /** A convenience method for matching violation groups. Enables syntax like:
    * 
    * ```
    * val result: Result = ...
    * result should failWith( group( "teacher", "is invalid",                // The group context
    *                                "firstName -> "must not be empty" ) )   // The rule violations
    * ```
    *
    * @param constraint A textual description of the constraint being violated (for example, "must not be empty").
    * @param description The textual description of the object under validation.
    * @param expectedViolations The set of expected violations that comprise the group.
    * @return A matcher over [[com.wix.accord.Results#GroupViolation]]s.
    */
  def group( description: String, constraint: Domain#Constraint, expectedViolations: ( String, Domain#Constraint )* ) =
    new GroupViolationMatcher( constraint  = constraint,
                               description = description,
                               violations  = ( expectedViolations map tupleToRuleMatcher ).toSet )

  /** Enables syntax like `someResult should be( aFailure )` */
  val aFailure = new BeMatcher[ Domain#Result ] {
    def apply( left: Domain#Result ) =
      MatchResult( left.isInstanceOf[ Domain#Failure ],
        s"$left is not a failure",
        s"$left is a failure"
      )
  }

  /** Enables syntax like `someResult should be( aSuccess )` */
  val aSuccess = new BeMatcher[ Domain#Result ] {
    def apply( left: Domain#Result ) = {
      MatchResult( left.isSuccess,
        s"$left is not a success (violations: ${left.failure.map { _.violations }.getOrElse( Set.empty ).mkString( ", " )})",
        s"$left is a success" )
    }
  }
}
