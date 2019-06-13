/*
  Copyright 2013-2019 Wix.com

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

import com.wix.accord.dsl.OrderingOps
import org.scalatest.{FlatSpec, Inside, Matchers}

import scala.collection.immutable.NumericRange


object OrderingOpsTests {
  sealed trait ArbitraryType

  //noinspection NotImplementedCode
  object ArbitraryType {
    implicit val integral = new Integral[ ArbitraryType ] {
      override def plus( x: ArbitraryType, y: ArbitraryType ): ArbitraryType = ???
      override def minus( x: ArbitraryType, y: ArbitraryType ): ArbitraryType = ???
      override def times( x: ArbitraryType, y: ArbitraryType ): ArbitraryType = ???
      override def negate( x: ArbitraryType ): ArbitraryType = ???
      override def fromInt( x: Int ): ArbitraryType = ???
      override def toInt( x: ArbitraryType ): Int = ???
      override def toLong( x: ArbitraryType ): Long = ???
      override def toFloat( x: ArbitraryType ): Float = ???
      override def toDouble( x: ArbitraryType ): Double = ???
      override def compare( x: ArbitraryType, y: ArbitraryType ): Int = ???
      override def quot( x: ArbitraryType, y: ArbitraryType ): ArbitraryType = ???
      override def rem( x: ArbitraryType, y: ArbitraryType ): ArbitraryType = ???
      def parseString( str: String ): Option[ ArbitraryType ] = ???   // Scala 2.13 extension
    }

    def apply() = new ArbitraryType {}
  }

  val lhs = ArbitraryType()
  val rhs = ArbitraryType()
  val lowerBound = ArbitraryType()
  val upperBound = ArbitraryType()
  val intLowerBound = 1
  val intUpperBound = 5
  val intRangeInclusive = intLowerBound to intUpperBound
  val intRangeExclusive = intLowerBound until intUpperBound
  val rangeInclusive = NumericRange.inclusive( lowerBound, upperBound, ArbitraryType() )
  val rangeExclusive = NumericRange( lowerBound, upperBound, ArbitraryType() )

  val snippet = "snippet"
  val ops = new OrderingOps { override def snippet = OrderingOpsTests.this.snippet }
}

class OrderingOpsTests extends FlatSpec with Matchers with Inside {
  import OrderingOpsTests._
  import com.wix.accord.combinators._

  "Operator \">\"" should "return a GreaterThan combinator" in {
    ( ops > rhs ) shouldEqual GreaterThan( rhs, snippet )
  }

  "Operator \">=\"" should "return a GreaterThanOrEqual combinator" in {
    ( ops >= rhs ) shouldEqual GreaterThanOrEqual( rhs, snippet )
  }

  "Operator \"<\"" should "return a LesserThan combinator" in {
    ( ops < rhs ) shouldEqual LesserThan( rhs, snippet )
  }

  "Operator \"<=\"" should "return a LesserThanOrEqual combinator" in {
    ( ops <= rhs ) shouldEqual LesserThanOrEqual( rhs, snippet )
  }

  "Operator \"==\"" should "return an EquivalentTo combinator" in {
    ( ops == rhs ) shouldEqual EquivalentTo( rhs, snippet )
  }

  "Operator \"between\"" should "return an InRangeInclusive combinator" in {
    ( ops between( lowerBound, upperBound ) ) shouldEqual InRangeInclusive( lowerBound, upperBound, snippet )
  }

  "Operator \"within\" over a native integer range" should "return an InRangeInclusive combinator" in {
    ( ops within intRangeInclusive ) shouldEqual InRangeInclusive( intLowerBound, intUpperBound, snippet )
  }

  "Operator \"within\" over a numeric range" should "return an InRangeInclusive combinator" in {
    ( ops within rangeInclusive ) shouldEqual InRangeInclusive( lowerBound, upperBound, snippet )
  }

  "Operator \"within\" over an exclusive native integer range" should "return an InRangeExclusive combinator" in {
    ( ops within intRangeExclusive ) shouldEqual InRangeExclusive( intLowerBound, intUpperBound, snippet )
  }

  "Operator \"within\" over an exclusive numeric range" should "return an InRangeExclusive combinator" in {
    ( ops within rangeExclusive ) shouldEqual InRangeExclusive( lowerBound, upperBound, snippet )
  }
}
