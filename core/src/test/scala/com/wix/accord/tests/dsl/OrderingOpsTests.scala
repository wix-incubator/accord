/*
  Copyright 2013-2015 Wix.com

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

import org.scalatest.{FlatSpec, Inside, Matchers}
import scala.collection.immutable.NumericRange


object OrderingOpsTests {
  sealed trait ArbitraryType
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
      override def quot(x: ArbitraryType, y: ArbitraryType): ArbitraryType = ???
      override def rem(x: ArbitraryType, y: ArbitraryType): ArbitraryType = ???
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


  // Validator rule definitions. These cannot be defined inline in the test spec because of conflicts
  // in the `should` extension method (it's defined both by the Accord DSL and the ScalaTest `Matchers`
  // trait).
  import com.wix.accord.dsl._

  val greaterThan             = lhs should be > rhs
  val greaterThanEqual        = lhs should be >= rhs
  val lesserThan              = lhs should be < rhs
  val lesserThanEqual         = lhs should be <= rhs
  val equivalentTo            = lhs should be == rhs
  val betweenBounds           = lhs is between( lowerBound, upperBound )
  val withinIntRangeInclusive = intLowerBound is within( intRangeInclusive )
  val withinIntRangeExclusive = intLowerBound is within( intRangeExclusive )
  val withinRangeInclusive    = lhs is within( rangeInclusive )
  val withinRangeExclusive    = lhs is within( rangeExclusive )
}

class OrderingOpsTests extends FlatSpec with Matchers with Inside {
  import com.wix.accord.combinators._
  import OrderingOpsTests._

  "The expression \"should be >\"" should "return a GreaterThan combinator" in {
    inside( greaterThan ) {
      case GreaterThan( bound, _ ) => bound shouldEqual rhs
    }
  }

  "The expression \"should be >=\"" should "return a GreaterThanOrEqual combinator" in {
    inside( greaterThanEqual ) {
      case GreaterThanOrEqual( bound, _ ) => bound shouldEqual rhs
    }
  }

  "The expression \"should be <\"" should "return a LesserThan combinator" in {
    inside( lesserThan ) {
      case LesserThan( bound, _ ) => bound shouldEqual rhs
    }
  }

  "The expression \"should be <=\"" should "return a LesserThanOrEqual combinator" in {
    inside( lesserThanEqual ) {
      case LesserThanOrEqual( bound, _ ) => bound shouldEqual rhs
    }
  }

  "The expression \"should be ==\"" should "return an EquivalentTo combinator" in {
    inside( equivalentTo ) {
      case EquivalentTo( bound, _ ) => bound shouldEqual rhs
    }
  }

  "The expression \"is between\"" should "return an InRangeInclusive combinator" in {
    inside( betweenBounds ) {
      case InRangeInclusive( lBound, uBound, _ ) =>
        lBound shouldEqual lowerBound
        uBound shouldEqual upperBound
    }
  }

  "The expression \"is within\" over a native integer range" should "return an InRangeInclusive combinator" in {
    inside( withinIntRangeInclusive ) {
      case InRangeInclusive( lBound, uBound, _ ) =>
        lBound shouldEqual intLowerBound
        uBound shouldEqual intUpperBound
    }
  }

  "The expression \"is within\" over a numeric range" should "return an InRangeInclusive combinator" in {
    inside( withinRangeInclusive ) {
      case InRangeInclusive( lBound, uBound, _ ) =>
        lBound shouldEqual lowerBound
        uBound shouldEqual upperBound
    }
  }

  "The expression \"is within\" over an exclusive native integer range" should "return an InRangeExclusive combinator" in {
    inside( withinIntRangeExclusive ) {
      case InRangeExclusive( lBound, uBound, _ ) =>
        lBound shouldEqual intLowerBound
        uBound shouldEqual intUpperBound
    }
  }

  "The expression \"is within\" over an exclusive numeric range" should "return an InRangeExclusive combinator" in {
    inside( withinRangeExclusive ) {
      case InRangeExclusive( lBound, uBound, _ ) =>
        lBound shouldEqual lowerBound
        uBound shouldEqual upperBound
    }
  }
}
