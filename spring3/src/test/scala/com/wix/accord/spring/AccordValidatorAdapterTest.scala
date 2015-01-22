/*
  Copyright 2013-2014 Wix.com

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

package com.wix.accord.spring

import org.scalatest.{WordSpec, Matchers}
import org.springframework.validation.{BeanPropertyBindingResult, Errors}
import scala.collection.JavaConversions._
import com.wix.accord.TestDomain

import AccordValidatorAdapterTest._

class AccordValidatorAdapterTest extends WordSpec with Matchers {

  "The validation adapter" should {
    def adapter = new AccordValidatorAdapter( TestDomain, testClassValidator )

    "find and apply an Accord validator in a companion object" in {
      val test = TestClass( "ok", 5 )
      val errors: Errors = new BeanPropertyBindingResult( test, "test" )
      adapter.validate( test, errors )

      errors.getAllErrors shouldBe empty
    }

    "correctly render a validation error" in {
      val test = TestClass( "not ok", 15 )
      val errors: Errors = new BeanPropertyBindingResult( test, "test" )
      adapter.validate( test, errors )

      errors.getAllErrors should have size 1
      errors.getAllErrors.head.getCode shouldEqual SpringAdapterBase.defaultErrorCode
      errors.getAllErrors.head.getDefaultMessage shouldEqual s"f2 $sizeConstraint"
    }

    "correctly render multiple validation errors" in {
      val test = TestClass( "not ok at all", 15 )
      val expectedErrorMessages = Seq(
        s"f1 $sizeConstraint",
        s"f2 $valueConstraint"
      )

      val errors: Errors = new BeanPropertyBindingResult( test, "test" )
      adapter.validate( test, errors )
      errors.getAllErrors should have size 2
      val returnedMessages = errors.getAllErrors map { _.getDefaultMessage }
      returnedMessages should contain only( expectedErrorMessages:_* )
    }
  }

}

private object AccordValidatorAdapterTest {
  case class TestClass( f1: String, f2: Int )

  import TestDomain.dsl._
  implicit val testClassValidator = validator[ TestClass ] { ae =>
    ae.f1 has size <= 10
    ae.f2 should be <= 10
  }
  val sizeConstraint = TestDomain.Constraints.LesserThanEqual( 10 )
  val valueConstraint = TestDomain.Constraints.LesserThanEqual( 10 )
}
