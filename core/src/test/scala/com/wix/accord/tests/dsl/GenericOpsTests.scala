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

import com.wix.accord.scalatest.ResultMatchers
import org.scalatest.{Matchers, WordSpec}
import com.wix.accord.combinators.{AnInstanceOf, EqualTo, IsNotNull, IsNull, NotAnInstanceOf, NotEqualTo}

class GenericOpsTests extends WordSpec with Matchers with ResultMatchers {
  import GenericOpsTests._
  
  "The expression \"is aNull\"" should {
    "return an IsNull combinator" in {
      aNullValidator shouldBe an[ IsNull ]
    }
  }

  "The expression \"is notNull\"" should {
    "return an IsNotNull combinator" in {
      notNullValidator shouldBe an[ IsNotNull ]
    }
  }

  "The expression \"is equalTo\"" should {
    "return an EqualTo combinator" in {
      equalToValidator shouldBe an[ EqualTo[_] ]
    }
  }

  "The expression \"is notEqualTo\"" should {
    "return an NotEqualTo combinator" in {
      notEqualToValidator shouldBe a[ NotEqualTo[_] ]
    }
  }

  "The expression \"is anInstanceOf\"" should {
    "return an AnInstanceOf combinator" in {
      anInstanceOfValidator shouldBe an[ AnInstanceOf[_] ]
    }
  }

  "The expression \"is notAnInstanceOf\"" should {
    "return a NotAnInstanceOf combinator" in {
      notAnInstanceOfValidator shouldBe a[ NotAnInstanceOf[_] ]
    }
  }

  "A \"conditional\" block" should {
    import com.wix.accord._
    import com.wix.accord.Descriptions._

    def extractViolation( result: Result ) = result match {
      // Not Set.unapply even for singletons, ugh
      case Failure( violations ) if violations.size == 1 && violations.head.isInstanceOf[ RuleViolation ] =>
        violations.head.asInstanceOf[ RuleViolation ]
      case _ =>
        throw new IllegalArgumentException( s"Unexpected result $result, expected a failure with a single RuleViolation" )
    }

    def extractConditional( result: Result ) = extractViolation( result ).rawDescription match {
      case c: Conditional => c
      case desc =>
        throw new IllegalArgumentException( s"Unexpected description $desc, expected a Conditional" )
    }

    "produce a Success if no condition applies" in {
      val result = guardedConditionalValidator( GuardedConditionalTest( 100, "test" ) )
      result shouldBe aSuccess
    }

    "dispatch correctly based on runtime value" in {
      val test1 = ConditionalTest( Value1, "test1" )
      val test2 = ConditionalTest( Value2, "test2" )
      val violation1 = extractViolation( conditionalValidator( test1 ) )
      val violation2 = extractViolation( conditionalValidator( test2 ) )
      violation1.value shouldEqual "test1"
      violation1.value shouldEqual "test2"
    }

    "include the runtime value" in {
      val result = conditionalValidator( ConditionalTest( Value1, "test" ) )
      val conditional = extractConditional( result )
      conditional.value shouldEqual Value1
    }

    "correctly describe the condition" in {
      val test = ConditionalTest( Value1, "test" )
      val conditional = extractConditional( conditionalValidator( test ) )
      conditional.on shouldEqual AccessChain( "cond" )
    }

    "correctly describe the validation target for the matching case" in {
      val test = ConditionalTest( Value1, "test" )
      val conditional = extractConditional( conditionalValidator( test ) )
      conditional.target shouldEqual AccessChain( "value" )
    }

    "correctly describe the matching case guard, if applicable" in {
      val test = GuardedConditionalTest( -15, "not good" )
      val conditional = extractConditional( guardedConditionalValidator( test ) )
      conditional.guard shouldEqual Some( Generic( "value < 0" ) )
    }
  }
}

object GenericOpsTests {
  import com.wix.accord.dsl._
  
  sealed trait Value
  case object Value1 extends Value
  case object Value2 extends Value
  
  val value: Value = Value2
  
  val aNullValidator = value is aNull
  val notNullValidator = value is notNull
  val equalToValidator = value is equalTo( Value1 )
  val notEqualToValidator = value is notEqualTo( Value1 )
  val anInstanceOfValidator = value is anInstanceOf[ Value1.type ]
  val notAnInstanceOfValidator = value is notAnInstanceOf[ Value1.type ]

  case class ConditionalTest( cond: Value, value: String )
  case class GuardedConditionalTest( cond: Int, value: String )

  val conditionalValidator = validator[ ConditionalTest ] { ct =>
    conditional( ct.cond ) {
      case Value1 => ct.value should be == "Value1"
      case Value2 => ct.value should be == "Value2"
    }
  }

  val guardedConditionalValidator = validator[ GuardedConditionalTest ] { gct =>
    conditional( gct.cond ) {
      case value if value < 0 => gct.value should startWith( "-" )
      case value if value == 0 => gct.value should be == "zero"
    }
  }
}