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

import com.wix.accord.dsl
import org.scalatest.{WordSpec, Matchers}
import org.springframework.validation.{FieldError, BeanPropertyBindingResult, Errors}
import scala.collection.JavaConversions._

import AccordValidatorAdapterTest._

class AccordValidatorAdapterTest extends WordSpec with Matchers {

  "The validation adapter" should {
    def adapter = new AccordValidatorAdapter( testClassValidator )

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
      errors.getAllErrors.head.getDefaultMessage shouldEqual "f2 got 15, expected 10 or less"
    }

    "correctly render multiple validation errors" in {
      val test = TestClass( "not ok at all", 15 )
      val expectedErrorMessages = Seq(
        "f1 has size 13, expected 10 or less",
        "f2 got 15, expected 10 or less"
      )

      val errors: Errors = new BeanPropertyBindingResult( test, "test" )
      adapter.validate( test, errors )
      errors.getAllErrors should have size 2
      val returnedMessages = errors.getAllErrors map { _.getDefaultMessage }
      returnedMessages should contain only( expectedErrorMessages:_* )
    }

    "correctly render nested validation errors" in {
      val contained = Contained( "not good!" )
      val container = Container( "ok", contained )

      val errors: Errors = new BeanPropertyBindingResult( container, "container" )
      val adapter = new AccordValidatorAdapter( containerValidator )
      adapter.validate( container, errors )
      errors.getAllErrors should have size 1
      errors.getAllErrors.head shouldBe a[ FieldError ]

      val fe = errors.getAllErrors.head.asInstanceOf[ FieldError ]
      fe.getField shouldEqual "contained"
      fe.getRejectedValue shouldEqual Contained( "not good!" )
      // What should actually go in here?
    }
  }

}

private object AccordValidatorAdapterTest {
  import dsl._

  case class TestClass( f1: String, f2: Int )
  implicit val testClassValidator = validator[ TestClass ] { ae =>
    ae.f1 has size <= 10
    ae.f2 should be <= 10
  }

  case class Contained( field: String )
  implicit val containedValidator = validator[ Contained ] { c =>
    c.field has ( size >= 2 and size <= 4 )
  }

  case class Container( field: String, contained: Contained )
  implicit val containerValidator = validator[ Container ] { c =>
    c.field is notEmpty
    c.contained is valid
  }
}
