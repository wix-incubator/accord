/*
  Copyright 2013 Wix.com

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

package com.wix.accord.tests.dsl

import org.scalatest.{WordSpec, Matchers}
import com.wix.accord._
import com.wix.accord.scalatest.ResultMatchers

object OrderingOpsDslSpec {
  import dsl._

  case class IntTest( i: Int )
  implicit val intTestValidator = validator[ IntTest ] { _.i should be > 0 }

  case class FloatTest( f: Float )
  implicit val floatTestValidator = validator[ FloatTest ] { _.f should be > 0.0f }

  case class DoubleTest( d: Double )
  implicit val doubleTestValidator = validator[ DoubleTest ] { _.d should be > 0.0d }

  case class BigDecimalTest( b: BigDecimal )
  implicit val bigDecimalTestValidator = validator[ BigDecimalTest ] { _.b should be > BigDecimal( 0 ) }

  case class StringTest( s: String )
  implicit val stringTestValidator = validator[ StringTest ] { _.s should be > "a" }

  case class OrderedThing( v: Int )
  implicit val orderingOfAThing = new Ordering[ OrderedThing ] {
    def compare( x: OrderedThing, y: OrderedThing ): Int = x.v - y.v
  }
  case class OrderedTest( o: OrderedThing )
  implicit val orderedTestValidator = validator[ OrderedTest ] { _.o should be > OrderedThing( 0 ) }
}

import OrderingOpsDslSpec._
class OrderingOpsDslSpec extends WordSpec with Matchers with ResultMatchers {

  "OrderingOps as exposed by dsl" should {

    // The following are all just sanity checks (the real tests are in tests.combinators.OrderingOpsSpec);
    // the primary purpose of this spec is to ensure that a call site with ordering-oriented DSL calls compiles
    // correctly.

    "correctly evaluate an int-based rule" in {
      validate( IntTest( 5 ) ) should be( aSuccess )
      validate( IntTest( 0 ) ) should be( aFailure )
    }

    "correctly evaluate a float-based rule" in {
      validate( FloatTest( 5.0f ) ) should be( aSuccess )
      validate( FloatTest( 0.0f ) ) should be( aFailure )
    }

    "correctly evaluate a double-based rule" in {
      validate( DoubleTest( 5.0d ) ) should be( aSuccess )
      validate( DoubleTest( 0.0d ) ) should be( aFailure )
    }

    "correctly evaluate a BigDecimal-based rule" in {
      validate( BigDecimalTest( BigDecimal( 5 ) ) ) should be( aSuccess )
      validate( BigDecimalTest( BigDecimal( 0 ) ) ) should be( aFailure )
    }

    "correctly evaluate a string-based rule" in {
      validate( StringTest( "b" ) ) should be( aSuccess )
      validate( StringTest( "a" ) ) should be( aFailure )
    }

    "correctly evaluate a rule on an arbitrary class with Ordering" in {
      validate( OrderedTest( OrderedThing( 5 ) ) ) should be( aSuccess )
      validate( OrderedTest( OrderedThing( 0 ) ) ) should be( aFailure )
    }
  }
}

