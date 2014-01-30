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

import org.scalatest.{Matchers, WordSpec}
import com.wix.accord._
import com.wix.accord.scalatest.ResultMatchers


object CompilationSanityTests {
  import dsl._

  case class Test( x: String )
  implicit val stringValidator = validator[ String ] { _ is notNull }
  implicit val adaptedValidator = stringValidator compose { ( t: Test ) => t.x }
}

class CompilationSanityTests extends WordSpec with Matchers with ResultMatchers {
  import CompilationSanityTests._

  "Validator" should {
    "be contravariant" in {
      validate( "test" ) should be( aSuccess )
    }
  }

  "Validator adapted via compose()" should {
    "succeed on a valid object" in {
      validate( Test( "test" ) ) should be( aSuccess )
    }
    "fail on an invalid object" in {
      validate( Test( null ) ) should be( aFailure )
    }

    "retain a sensible description after adaptation" in {
      validate( Test( null ) ) should failWith( "x" -> "is a null" )
    }
  }
}
