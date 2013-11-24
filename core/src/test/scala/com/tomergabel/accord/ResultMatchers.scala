/*
  Copyright 2013 Tomer Gabel

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

package com.tomergabel.accord

import scala.language.implicitConversions
import org.scalatest.Suite
import org.scalatest.matchers.{BeMatcher, MatchResult, Matcher}

/** Extends a test suite with a set of matchers over validation [[com.tomergabel.accord.Result]]s. */
trait ResultMatchers {
  self: Suite =>

  sealed trait ViolationMatcher extends Matcher[ Violation ]

  case class RuleViolationMatcher( value: Any = null, constraint: String = null, context: String = null )
    extends ViolationMatcher {

    require( value != null || constraint != null || context != null )

    def apply( left: Violation ): MatchResult = left match {
      case rv: RuleViolation =>
        MatchResult(
          matches = ( value      == null || rv.value      == value      ) &&
                    ( constraint == null || rv.constraint == constraint ) &&
                    ( context    == null || rv.description    == context ),
          s"Rule violation $rv did not match pattern $this",
          s"Got unexpected rule violation $rv"
        )
      case _ =>
        MatchResult( matches = false,
          s"Unexpected violation '$left', " +
          s"expected a rule violation", s"Got unexpected rule violation '$left'" )
    }

    override def toString() = Seq( Option( value      ) getOrElse "_",
                                   Option( constraint ) getOrElse "_",
                                   Option( context    ) getOrElse "_" ).mkString( "RuleViolation(", ", ", ")" )

  }

  case class GroupViolationMatcher( value: Any = null, constraint: String = null, context: String = null,
                                    violations: Seq[ ViolationMatcher ] = null )
    extends ViolationMatcher {

    require( value != null || constraint != null || context != null && violations != null )

    def apply( left: Violation ): MatchResult = left match {
      case gv: GroupViolation =>
        val rulesMatch = gv.children.length == violations.length &&
                         gv.children.forall( rule => violations.exists( _.apply( rule ).matches ) )
        MatchResult(
          matches = ( value      == null || gv.value      == value      ) &&
                    ( constraint == null || gv.constraint == constraint ) &&
                    ( context    == null || gv.description    == context    ) &&
                    rulesMatch,
          s"Group violation $gv did not match pattern $this",
          s"Got unexpected group violation $gv"
        )
      case _ =>
        MatchResult( matches = false,
          s"Unexpected violation '$left', expected a group violation",
          s"Got unexpected group violation '$left'" )
    }

    override def toString() = Seq( Option( value      ) getOrElse "_",
                                   Option( constraint ) getOrElse "_",
                                   Option( context    ) getOrElse "_",
                                   Option( violations ) getOrElse "_" ).mkString( "GroupViolation(", ", ", ")" )
  }

  case class ResultMatcher( expectedViolations: Seq[ ViolationMatcher ] ) extends Matcher[ Result ] {
    def apply( left: Result ) = left match {
      case Success =>
        MatchResult( matches = false, "Validation was successful", "Validation was not successful" )

      case Failure( violations ) =>
        val matched = violations.map { v => ( v, expectedViolations.find( _.apply( v ).matches ) ) }
        val unexpected = matched collect { case ( v, None ) => v }
        val unmatched = expectedViolations.diff( matched collect { case ( _, Some( rule ) ) => rule } )

        MatchResult( matches = unexpected.isEmpty && unmatched.isEmpty,
          s"""
             |Validation failed!
             |Unexpected violations:
             |${unexpected.mkString( "\t", "\n\t", "" )}
             |Expected violations that weren't found:
             |${unmatched.mkString( "\t", "\n\t", "" )}
           """.stripMargin,
          "Stub negated message (negations not supported yet)"   // TODO complete this? Not sure negation makes sense in this context
        )
    }
  }

  def failWith( expectedViolations: ViolationMatcher* ): Matcher[ Result ] = ResultMatcher( expectedViolations )


  implicit def stringTuple2RuleMatcher( v: ( String, String ) ) =
    RuleViolationMatcher( context = v._1, constraint = v._2 )

  def group( context: String, constraint: String, expectedViolations: ( String, String )* ) =
    new GroupViolationMatcher( constraint = constraint,
                               context    = context,
                               violations = expectedViolations map stringTuple2RuleMatcher )

  /** Enables syntax like `someResult should be( aFailure )` */
  val aFailure = new BeMatcher[ Result ] {
    def apply( left: Result ) = MatchResult( left.isInstanceOf[ Failure ], "not a failure", "is a failure" )
  }

  /** Enables syntax like `someResult should be( aSuccess )` */
  val aSuccess = new BeMatcher[ Result ] {
    def apply( left: Result ) = {
      val violations = left match {
        case Success => Seq.empty
        case f: Failure => f.violations
      }
      MatchResult( violations.isEmpty, s"not a success (violations: ${violations.mkString( ", " )})", "is a success" )
    }
  }
}
