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

package com.tomergabel.accord.tests.combinators

import org.scalatest.Matchers
import com.tomergabel.accord.combinators.OrderingOps

class OrderingOpsSpec extends CombinatorTestSpec with Matchers {

  // Some arbitrary test class with its own implementation of Ordering
  case class Test( v: Int )
  implicit val testOrdering = new Ordering[ Test ] {
    def compare( x: Test, y: Test ): Int = x.v - y.v
  }

  "Base OrderingOps" should {
    val ops = new OrderingOps

    "successfully validate a greater-than rule" in {
      val left = Test( 10 )
      val validator = ops > Test( 5 )
      validator( left ) should be( aSuccess )
    }
    "render a correct greater-than rule violation" in {
      val left = Test( 0 )
      val validator = ops > Test( 5 )
      validator( left ) should failWith( testContext -> "got Test(0), expected more than Test(5)" )
    }
    "successfully validate a greater-than-or-equal rule" in {
      val left = Test( 10 )
      val validator = ops >= Test( 10 )
      validator( left ) should be( aSuccess )
    }
    "render a correct greater-than-or-equal rule violation" in {
      val left = Test( 0 )
      val validator = ops >= Test( 5 )
      validator( left ) should failWith( testContext -> "got Test(0), expected Test(5) or more" )
    }
    "successfully validate a lesser-than rule" in {
      val left = Test( 5 )
      val validator = ops < Test( 10 )
      validator( left ) should be( aSuccess )
    }
    "render a correct lesser-than rule violation" in {
      val left = Test( 10 )
      val validator = ops < Test( 10 )
      validator( left ) should failWith( testContext -> "got Test(10), expected less than Test(10)" )
    }
    "successfully validate a lesser-than-or-equal rule" in {
      val left = Test( 10 )
      val validator = ops <= Test( 10 )
      validator( left ) should be( aSuccess )
    }
    "render a correct lesser-than-or-equal rule violation" in {
      val left = Test( 10 )
      val validator = ops <= Test( 5 )
      validator( left ) should failWith( testContext -> "got Test(10), expected Test(5) or less" )
    }
    "successfully validate an equality rule" in {
      val left = Test( 10 )
      val validator = ops.==( Test( 10 ) )
      validator( left ) should be( aSuccess )
    }
    "render a correct equality rule violation" in {
      val left = Test( 10 )
      val validator = ops.==( Test( 5 ) )
      validator( left ) should failWith( testContext -> "got Test(10), expected Test(5)" )
    }
  }
  
  "OrderingOps with specific snippet" should {
    val ops = new OrderingOps( "has value" )

    "render a correctly modified rule violation" in {
      val left = Test( 0 )
      val validator = ops > Test( 5 )
      validator( left ) should failWith( testContext -> "has value Test(0), expected more than Test(5)" )
    }
  }
}
