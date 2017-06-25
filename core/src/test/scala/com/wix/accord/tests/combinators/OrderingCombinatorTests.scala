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

import com.wix.accord.Validator
import org.scalatest.Matchers
import com.wix.accord.combinators.OrderingCombinators
import com.wix.accord.scalatest.CombinatorTestSpec

class OrderingCombinatorTests extends CombinatorTestSpec with Matchers with OrderingCombinators {

  // Some arbitrary test class with its own implementation of Ordering
  case class Test( v: Int )
  implicit val testOrdering = new Ordering[ Test ] {
    def compare( x: Test, y: Test ): Int = x.v - y.v
  }

  "GreaterThan combinator" should {
    val validator = GreaterThan( Test( 5 ), "got" )

    "successfully validate a greater object" in {
      validator( Test( 10 ) ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      validator( Test( 0 ) ) should failWith( "got Test(0), expected more than Test(5)" )
    }
    "be null safe" in {
      validator( null ) shouldEqual Validator.nullFailure
    }
  }
  
  "GreaterThanOrEqual combinator" should {
    val validator = GreaterThanOrEqual( Test( 10 ), "got" )

    "successfully validate a greater object" in {
      validator( Test( 20 ) ) should be( aSuccess )
    }
    "successfully validate an equal object" in {
      validator( Test( 10 ) ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      validator( Test( 0 ) ) should failWith( "got Test(0), expected Test(10) or more" )
    }
    "be null safe" in {
      validator( null ) shouldEqual Validator.nullFailure
    }
  }
  
  "LesserThan combinator" should {
    val validator = LesserThan( Test( 10 ), "got" )

    "successfully validate a lesser object" in {
      validator( Test( 5 ) ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      validator( Test( 10 ) ) should failWith( "got Test(10), expected less than Test(10)" )
    }
    "be null safe" in {
      validator( null ) shouldEqual Validator.nullFailure
    }
  }
  
  "LesserThanOrEqual combinator" should {
    val validator = LesserThanOrEqual( Test( 10 ), "got" )

    "successfully validate a lesser object" in {
      validator( Test( 5 ) ) should be( aSuccess )
    }
    "successfully validate an equal object" in {
      validator( Test( 10 ) ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      validator( Test( 20 ) ) should failWith( "got Test(20), expected Test(10) or less" )
    }
    "be null safe" in {
      validator( null ) shouldEqual Validator.nullFailure
    }
  }
  
  "EquivalentTo combinator" should {
    val validator = EquivalentTo( Test( 10 ), "got" )

    "successfully validate an equal object" in {
      validator( Test( 10 ) ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      validator( Test( 5 ) ) should failWith( "got Test(5), expected Test(10)" )
    }
    "be null safe" in {
      validator( null ) shouldEqual Validator.nullFailure
    }
  }
  
  "InRangeInclusive combinator" should {
    val validator = InRangeInclusive( Test( 5 ), Test( 10 ), "got" )

    "successfully validate an object within the specified range" in {
      validator( Test( 10 ) ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      validator( Test( 1 ) ) should failWith( "got Test(1), expected between Test(5) and Test(10)" )
    }
    "be null safe" in {
      validator( null ) shouldEqual Validator.nullFailure
    }
  }
  
  "InRangeExclusive combinator" should {
    val validator = InRangeExclusive( Test( 5 ), Test( 10 ), "got" ).exclusive

    "successfully validate an object within the specified range" in {
      validator( Test( 5 ) ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      validator( Test( 10 ) ) should failWith( "got Test(10), expected between Test(5) and Test(10) (exclusively)" )
    }
    "be null safe" in {
      validator( null ) shouldEqual Validator.nullFailure
    }
  }
}
