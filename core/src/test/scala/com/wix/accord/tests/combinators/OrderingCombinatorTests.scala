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

import org.scalatest.Matchers
import com.wix.accord.combinators.OrderingCombinators
import com.wix.accord.scalatest.CombinatorTestSpec

class OrderingCombinatorTests extends CombinatorTestSpec with Matchers {

  val combinators = new OrderingCombinators {}
  import combinators._

  // Some arbitrary test class with its own implementation of Ordering
  case class Test( v: Int )
  implicit val testOrdering = new Ordering[ Test ] {
    def compare( x: Test, y: Test ): Int = x.v - y.v
  }

  val zero = Test( 0 )
  val one = Test( 1 )
  val ten = Test( 10 )
  val twenty = Test( 20 )

  "GreaterThan combinator" should {
    val greaterThanTen = new GreaterThan( ten, "got" )

    "successfully validate a greater object" in {
      greaterThanTen( twenty ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      greaterThanTen( one ) should failWith( GreaterThanConstraint( ten, "got", one ) )
    }
  }
  
  "GreaterThanOrEqual combinator" should {
    val greaterThanOrEqualToTen = new GreaterThanOrEqual( ten, "got" )

    "successfully validate a greater object" in {
      greaterThanOrEqualToTen( twenty ) should be( aSuccess )
    }
    "successfully validate an equal object" in {
      greaterThanOrEqualToTen( ten ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      greaterThanOrEqualToTen( one ) should failWith( GreaterThanOrEqualConstraint( ten, "got", one ) )
    }
  }
  
  "LesserThan combinator" should {
    val lesserThanTen = new LesserThan( ten, "got" )

    "successfully validate a lesser object" in {
      lesserThanTen( one ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      lesserThanTen( twenty ) should failWith( LesserThanConstraint( ten, "got", twenty ) )
    }
  }
  
  "LesserThanOrEqual combinator" should {
    val lesserThanOrEqualToTen = new LesserThanOrEqual( ten, "got" )

    "successfully validate a lesser object" in {
      lesserThanOrEqualToTen( one ) should be( aSuccess )
    }
    "successfully validate an equal object" in {
      lesserThanOrEqualToTen( ten ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      lesserThanOrEqualToTen( twenty ) should failWith( LesserThanOrEqualConstraint( ten, "got", twenty ) )
    }
  }
  
  "EquivalentTo combinator" should {
    val equivalentToTen = new EquivalentTo( ten, "got" )

    "successfully validate an equal object" in {
      equivalentToTen( ten ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      equivalentToTen( one ) should failWith( EquivalentToConstraint( ten, "got", one ) )
    }
  }
  
  "InRangeInclusive combinator" should {
    val zeroToTen = new InRangeInclusive( zero, ten, "got" )

    "successfully validate an object within the specified range" in {
      zeroToTen( one ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      zeroToTen( twenty ) should failWith( InRangeInclusiveConstraint( "got", zero, ten, twenty ) )
    }
  }
  
  "InRangeExclusive combinator" should {
    val zeroUntilTen = new InRangeExclusive( zero, ten, "got" ).exclusive

    "successfully validate an object within the specified range" in {
      zeroUntilTen( one ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      zeroUntilTen( ten ) should failWith( InRangeExclusiveConstraint( "got", zero, ten, ten ) )
    }
  }
}
