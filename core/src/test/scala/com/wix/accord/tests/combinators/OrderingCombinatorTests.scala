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

class OrderingCombinatorTests extends CombinatorTestSpec with Matchers with OrderingCombinators {

  // Some arbitrary test class with its own implementation of Ordering
  case class Test( v: Int )
  implicit val testOrdering = new Ordering[ Test ] {
    def compare( x: Test, y: Test ): Int = x.v - y.v
  }

  "GreaterThan combinator" should {
    "successfully validate a greater object" in {
      val left = Test( 10 )
      val validator = new GreaterThan( Test( 5 ), "got" )
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = Test( 0 )
      val validator = new GreaterThan( Test( 5 ), "got" )
      validator( left ) should failWith( "got Test(0), expected more than Test(5)" )
    }
  }
  
  "GreaterThanOrEqual combinator" should {
    "successfully validate a greater object" in {
      val left = Test( 20 )
      val validator = new GreaterThanOrEqual( Test( 10 ), "got" )
      validator( left ) should be( aSuccess )
    }
    "successfully validate an equal object" in {
      val left = Test( 10 )
      val validator = new GreaterThanOrEqual( Test( 10 ), "got" )
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = Test( 0 )
      val validator = new GreaterThanOrEqual( Test( 5 ), "got" )
      validator( left ) should failWith( "got Test(0), expected Test(5) or more" )
    }
  }
  
  "LesserThan combinator" should {
    "successfully validate a lesser object" in {
      val left = Test( 5 )
      val validator = new LesserThan( Test( 10 ), "got" )
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = Test( 10 )
      val validator = new LesserThan( Test( 10 ), "got" )
      validator( left ) should failWith( "got Test(10), expected less than Test(10)" )
    }
  }
  
  "LesserThanOrEqual combinator" should {
    "successfully validate a lesser object" in {
      val left = Test( 5 )
      val validator = new LesserThanOrEqual( Test( 10 ), "got" )
      validator( left ) should be( aSuccess )
    }
    "successfully validate an equal object" in {
      val left = Test( 10 )
      val validator = new LesserThanOrEqual( Test( 10 ), "got" )
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = Test( 10 )
      val validator = new LesserThanOrEqual( Test( 5 ), "got" )
      validator( left ) should failWith( "got Test(10), expected Test(5) or less" )
    }
  }
  
  "EquivalentTo combinator" should {
    "successfully validate an equal object" in {
      val left = Test( 10 )
      val validator = new EquivalentTo( Test( 10 ), "got" )
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = Test( 10 )
      val validator = new EquivalentTo( Test( 5 ), "got" )
      validator( left ) should failWith( "got Test(10), expected Test(5)" )
    }
  }
  
  "InRangeInclusive combinator" should {
    "successfully validate an object within the specified range" in {
      val left = Test( 10 )
      val validator = new InRangeInclusive( Test( 5 ), Test( 10 ), "got" )
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = Test( 1 )
      val validator = new InRangeInclusive( Test( 5 ), Test( 10 ), "got" )
      validator( left ) should failWith( "got Test(1), expected between Test(5) and Test(10)" )
    }
  }
  
  "InRangeExclusive combinator" should {
    "successfully validate an object within the specified range" in {
      val left = Test( 5 )
      val validator = new InRangeExclusive( Test( 5 ), Test( 10 ), "got" ).exclusive
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = Test( 10 )
      val validator = new InRangeExclusive( Test( 5 ), Test( 10 ), "got" ).exclusive
      validator( left ) should failWith( "got Test(10), expected between Test(5) and Test(10) (exclusively)" )
    }
  }
}
