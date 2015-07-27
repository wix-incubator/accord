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
import org.scalatest.{WordSpec, Matchers}
import com.wix.accord.Validator

class OptionOpsTests extends WordSpec with Matchers with ResultMatchers {
  import OptionOpsTests._

  // Quick and dirty helper to make the tests a little more terse
  implicit class EnrichOptionOfString( test: Option[ String ] ) {
    def validatedWith( validator: Validator[ Test ] ) = validator apply Test( test )
  }

  "Option.each" should {
    "succeed on None" in {
      None validatedWith eachValidator should be( aSuccess )
    }
    "succeed on Some that matches the predicate" in {
      Some( "test string" ) validatedWith eachValidator should be( aSuccess )
    }
    "fail on Some that does not match the predicate" in {
      Some( "some string" ) validatedWith eachValidator should be( aFailure )
    }
  }

  "empty" should {
    "succeed on None" in {
      None validatedWith emptyValidator should be( aSuccess )
    }
    "fail on Some" in {
      Some( "test" ) validatedWith emptyValidator should be( aFailure )
    }
  }

  "notEmpty" should {
    "succeed on Some" in {
      Some( "test" ) validatedWith notEmptyValidator should be( aSuccess )
    }
    "fail on None" in {
      None validatedWith notEmptyValidator should be( aFailure )
    }
  }

  "Option.get" should {
    "throw NoSuchElementException on None" in {
      a [ NoSuchElementException ] should be thrownBy { None validatedWith explicitGetValidator }
    }
    "succeed on Some that matches the predicate" in {
      Some( "test string" ) validatedWith eachValidator should be( aSuccess )
    }
    "fail on Some that does not match the predicate" in {
      Some( "some string" ) validatedWith eachValidator should be( aFailure )
    }
  }
}

object OptionOpsTests {
  case class Test( o: Option[ String ] )

  import com.wix.accord.dsl._
  val eachValidator = validator[ Test ] {  _.o.each should startWith( "test" ) }
  val emptyValidator = validator[ Test ] {  _.o is empty }
  val notEmptyValidator = validator[ Test ] {  _.o is notEmpty }
  val explicitGetValidator = validator[ Test ] { _.o.get should startWith( "test" ) }
}