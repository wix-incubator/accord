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

import com.wix.accord._
import org.scalatest.{WordSpec, Matchers}
import com.wix.accord.scalatest.ResultMatchers

class CompanionObjectAccordValidatorResolverTest extends WordSpec with Matchers with ResultMatchers {

  import CompanionObjectAccordValidatorResolverTest._

  "CompanionObjectAccordResolver" should {

    def resolver = new CompanionObjectAccordValidatorResolver

    "successfully resolve a validator when available on a test class companion" in {
      val resolved = resolver.lookupValidator[ Test1 ]
      resolved should not be empty

      // Quick sanity tests to make sure the resolved validator is functional
      implicit val validator = resolved.get
      val t1 = Test1( 10 )
      val t2 = Test1( 1 )
      validate( t1 ) shouldBe aSuccess
      validate( t2 ) shouldBe aFailure
    }

    "return None when no validator is available on the test class companion" in {
      val resolved = resolver.lookupValidator[ Test2 ]
      resolved shouldBe empty
    }
  }

}

object CompanionObjectAccordValidatorResolverTest {
  import dslcontext._

  case class Test1( f: Int )
  case object Test1 {
    implicit val validatorOfTest1 = validator[ Test1 ] { t => t.f should be > 5 }
  }

  case class Test2( x: String )
  case object Test2
}

