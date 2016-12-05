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

package com.wix.accord.tests.combinators

import com.wix.accord.combinators.{And, Fail, IsFalse, IsTrue, NilValidator, Or}
import com.wix.accord.scalatest.CombinatorTestSpec

class BooleanCombinatorTests extends CombinatorTestSpec {

  "IsTrue combinator" should {
    "successfully validate the value true" in {
      val left = true
      val validator = new IsTrue
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = false
      val validator = new IsTrue
      validator( left ) should failWith( "must be true" )
    }
  }

  "IsFalse combinator" should {
    "successfully validate the value false" in {
      val left = false
      val validator = new IsFalse
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = true
      val validator = new IsFalse
      validator( left ) should failWith( "must be false" )
    }
  }

  val failureClause = new Fail[ String ]( "expected failure" )
  val successClause = new NilValidator[ String ]

  "And combinator" should {
    "succeed on a two-clause expression when all clauses validate successfully" in {
      val validator = new And( successClause, successClause )
      validator( "test" ) should be( aSuccess )
    }
    "fail on a two-clause expression when the first clause fails to validate" in {
      val validator = new And( failureClause, successClause )
      validator( "test" ) should be( aFailure )
    }
    "fail on a two-clause expression when the second clause fails to validate" in {
      val validator = new And( successClause, failureClause )
      validator( "test" ) should be( aFailure )
    }
    "fail on a two-clause expression when both clauses fail to validate" in {
      val validator = new And( failureClause, failureClause )
      validator( "test" ) should be( aFailure )
    }
  }

  "Or combinator" should {
    "succeed on a two-clause expression when all clauses validate successfully" in {
      val validator = new Or( successClause, successClause )
      validator( "test" ) should be( aSuccess )
    }
    "succeed on a two-clause expression when only the first clause fails to validate" in {
      val validator = new Or( failureClause, successClause )
      validator( "test" ) should be( aSuccess )
    }
    "succeed on a two-clause expression when only the second clause fails to validate" in {
      val validator = new Or( successClause, failureClause )
      validator( "test" ) should be( aSuccess )
    }
    "fail on a two-clause expression when both clauses fail to validate" in {
      val validator = new Or( failureClause, failureClause )
      validator( "test" ) should be( aFailure )
    }
  }
}
