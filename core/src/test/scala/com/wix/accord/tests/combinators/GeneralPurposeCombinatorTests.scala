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

import com.wix.accord.combinators.GeneralPurposeCombinators
import com.wix.accord.BaseValidator
import com.wix.accord.scalatest.CombinatorTestSpec

class GeneralPurposeCombinatorTests extends CombinatorTestSpec {

  val combinators = new GeneralPurposeCombinators {}
  import combinators._

  "Fail combinator" should {
    "render a correct rule violation" in {
      val validator = new Fail[ String ]( "message" )
      validator( "whatever" ) should failWith( "message" )
    }
  }

  "Nil validator" should {
    "successfully validate an arbitrary object" in {
      val validator = new NilValidator[ String ]
      validator( "whatever" ) should be( aSuccess )
    }
  }

  "EqualTo validator" should {
    "successfully validate an equal object" in {
      val validator = new EqualTo[ String ]( "test" )
      validator( "test" ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val validator = new EqualTo[ String ]( "test" )
      validator( "invalid" ) should failWith( EqualToConstraint( "test" ) )
    }
  }

  "NotEqualTo validator" should {
    "successfully validate an unequal object" in {
      val validator = new NotEqualTo[ String ]( "test" )
      validator( "other" ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val validator = new NotEqualTo[ String ]( "test" )
      validator( "test" ) should failWith( NotEqualToConstraint( "test" ) )
    }
  }

  "IsNull validator" should {
    "successfully validate a null of an arbitrary type" in {
      val validator = new IsNull
      validator( null ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val validator = new IsNull
      validator( "test" ) should failWith( IsNullConstraint )
    }
  }

  "IsNotNull validator" should {
    "successfully validate an instance of an arbitrary type" in {
      val validator = new IsNotNull
      validator( "test" ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val validator = new IsNotNull
      validator( null ) should failWith( IsNotNullConstraint )
    }
  }

  "Valid validator" should {
    "be null-safe" in {
      import com.wix.accord.ViolationBuilder._
      case class Test( f: String )
      implicit val delegate = new BaseValidator[ Test ]( _.f == "anything", _ -> "just a safety net, shouldn't happen" )

      val validator = new Valid[ Test ]
      validator( null ) should failWith( IsNullConstraint )
    }
  }

  "AnInstanceOf validator" should {
    "successfully validate an object of the correct type" in {
      val validator = new AnInstanceOf[ String ]
      validator( "test" ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val validator = new AnInstanceOf[ BigDecimal ]
      validator( "invalid" ) should failWith( AnInstanceOfConstraint[ BigDecimal ] )
    }
  }

  "NotAnInstanceOf validator" should {
    "successfully validate an object of a differnet type" in {
      val validator = new NotAnInstanceOf[ BigDecimal ]
      validator( "test" ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val validator = new NotAnInstanceOf[ String ]
      validator( "invalid" ) should failWith( NotAnInstanceOfConstraint[ String ] )
    }
  }

  import com.wix.accord.ViolationBuilder._
  sealed trait TestResult
  case object FailOne extends TestResult
  case object FailTwo extends TestResult
  case object FailBoth extends TestResult
  case object SatisfyBoth extends TestResult
  case object ConstraintOne
  case object ConstraintTwo
  case object ClauseOne extends BaseValidator[ TestResult ]( r => r != FailOne && r != FailBoth, _ -> ConstraintOne )
  case object ClauseTwo extends BaseValidator[ TestResult ]( r => r != FailTwo && r != FailBoth, _ -> ConstraintTwo )

  "And combinator with a two-clause rule" should {
    val validator = new And( ClauseOne, ClauseTwo )

    "successfully validate a object that satisfies both clauses" in {
      validator( SatisfyBoth ) should be( aSuccess )
    }
    "render a correct rule violation when the first clause is not satisfied" in {
      validator( FailOne ) should failWith( ConstraintOne )
    }
    "render a correct rule violation when the second clause is not satisfied" in {
      validator( FailTwo ) should failWith( ConstraintTwo )
    }
    "render a correct rule violation when both clauses are not satisfied" in {
      validator( FailBoth ) should failWith( ConstraintOne, ConstraintTwo )
    }
  }

  "Or combinator with a two-clause rule" should {
    val validator = new Or( ClauseOne, ClauseTwo )

    "successfully validate a object that satisfies both clauses" in {
      validator( SatisfyBoth ) should be( aSuccess )
    }
    "successfully validate an object that only satisfies the first clause" in {
      validator( FailTwo ) should be( aSuccess )
    }
    "successfully validate an object that only satisfies the second clause" in {
      validator( FailOne ) should be( aSuccess )
    }
    "render a correct rule violation when both clauses are not satisfied" in {
      validator( FailBoth ) should failWith( GroupViolationMatcher(
        constraint = OrConstraint,
        violations = Set( ClauseOne, ClauseTwo )
      ) )
    }
  }

}
