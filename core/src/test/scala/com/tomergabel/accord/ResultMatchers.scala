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

import org.scalatest.Suite
import org.scalatest.matchers.{BeMatcher, MatchResult, Matcher}

/** Extends a test suite with a set of matchers over validation [[com.tomergabel.accord.Result]]s. */
trait ResultMatchers {
  self: Suite =>


  // TODO update documentation
  /** Enables syntax like `someResult should failWith( "violation1", "violation2", ... )`.
    * Unspecified violations will fail the test with an "unexpected violations" message. Specified
    * violations that did not occur will fail the tests with an "expected violations weren't found"
    * message.
    */
  def failRule( expectedViolations: ( String, String )* ) = new Matcher[ Result ] {
    def apply( left: Result ): MatchResult =
      left match {
        case Success => MatchResult( matches = false, "Validation was successful", "Validation was not successful" )
        case Failure( vlist ) =>
          val violations = vlist.flatten.map { v => ( v.description, v.constraint ) }.toSet
          val remainder = expectedViolations.toSet -- violations
          val unexpected = violations.toSet -- expectedViolations.toSet
          MatchResult( matches = remainder.isEmpty && unexpected.isEmpty,
            "Validation failed with unexpected violations!\nExpected violations that weren't found:\n"
              + remainder.mkString( "\t", "\n\t", "\n" )
              + "Unexpected violations:\n" + unexpected.mkString( "\t", "\n\t", "\n" ),
            // How to negate?
            "Validation failed with unexpected violations!\nExpected violations that weren't found:\n"
              + remainder.mkString( "\t", "\n\t", "\n" )
              + "Unexpected violations:\n" + unexpected.mkString( "\t", "\n\t", "\n" )
          )
      }
  }

  /** Enables syntax like `someResult should be( aFailure )` */
  val aFailure = new BeMatcher[ Result ] {
    def apply( left: Result ) = MatchResult( left.isInstanceOf[ Failure ], "not a failure", "is a failure" )
  }

  /** Enables syntax like `someResult should be( aSuccess )` */
  val aSuccess = new BeMatcher[ Result ] {
    def apply( left: Result ) = MatchResult( left == Success, "not a success", "is a success" )
  }
}
