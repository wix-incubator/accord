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

package com.tomergabel.accord.tests.dsl

import org.scalatest.{WordSpec, Matchers}
import com.tomergabel.accord._

object DescriptionEnabledValidationTests {
  import dsl._

  case class Described( value: String )

  implicit val describedValidator = validator[ Described ] { t =>
    t.value as "described" is notEmpty
  }
}

import DescriptionEnabledValidationTests._
class DescriptionEnabledValidationTests extends WordSpec with Matchers with ResultMatchers {

  "Validator invocation on an explicitly described field" should {
    "succeed on a valid value" in {
      validate( Described( "ok" ) ) should be( aSuccess )
    }

    "fail on an invalid value" in {
      validate( Described( "" ) ) should be( aFailure )
    }

    "generate a correct violation message" in {
      validate( Described( "" ) ) should failWith( "described" -> "must not be empty" )
    }
  }
}
