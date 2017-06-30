/*
  Copyright 2013-2017 Wix.com

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

package com.wix.accord

import org.scalatest.{Matchers, WordSpec}
import org.scalatest.LoneElement._    // Compilation fails when mixing this in, no idea why.

class BaseValidatorTests extends WordSpec with Matchers with ViolationBuilder {
  "BaseValidator.report" should {

    val validator = new NullSafeValidator[ String ]( _ startsWith "ok", _ -> "no good" )
    "return a Failure with a default violation on nulls" in {
      val result = validator.apply( null )
      result shouldBe a[ Failure ]
      val Failure( violations ) = result
      violations.loneElement shouldEqual RuleViolation( null, "is a null" )
    }

    "return a Success if test succeeds" in {
      validator.apply( "ok" ) shouldEqual Success
    }

    "return a Failure with the generated violations if test fails" in {
      val result = validator.apply( "no" )
      result shouldBe a[ Failure ]
      val Failure( violations ) = result
      violations.loneElement shouldEqual RuleViolation( "no", "no good" )
    }
  }
}
