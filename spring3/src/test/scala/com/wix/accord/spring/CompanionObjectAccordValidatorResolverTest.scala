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

package com.wix.accord.spring

import com.wix.accord._
import org.scalatest.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.wix.accord.scalatest.ResultMatchers

trait CompanionObjectValidatorResolverBehaviors extends Matchers with ResultMatchers {
  this: AnyWordSpec =>

  import CompanionObjectAccordValidatorResolverTest._

  def companionObjectValidatorResolver( newResolver: => AccordValidatorResolver ) = {

    "successfully resolve a validator when available on a test class companion" in {
      val resolved = newResolver.lookupValidator[ Test1 ]
      resolved should not be empty

      // Quick sanity tests to make sure the resolved validator is functional
      implicit val validator = resolved.get
      val t1 = Test1( 10 )
      val t2 = Test1( 1 )
      validate( t1 ) shouldBe aSuccess
      validate( t2 ) shouldBe aFailure
    }

    "return None when no validator is available on the test class companion" in {
      val resolved = newResolver.lookupValidator[ Test2 ]
      resolved shouldBe empty
    }

    "return None when the test class has no companion object" in {
      val resolved = newResolver.lookupValidator[ Test3 ]
      resolved shouldBe empty
    }
  }
}

class CompanionObjectAccordValidatorResolverTest extends AnyWordSpec with CompanionObjectValidatorResolverBehaviors {

  "CompanionObjectAccordValidatorResolver" should {
    behave like companionObjectValidatorResolver( new CompanionObjectAccordValidatorResolver )
  }

  "CachingCompanionObjectAccordValidatorResolver" should {
    behave like companionObjectValidatorResolver( new CachingCompanionObjectAccordValidatorResolver )
  }
}

object CompanionObjectAccordValidatorResolverTest {
  import dsl._

  case class Test1( f: Int )
  case object Test1 {
    implicit val validatorOfTest1 = validator[ Test1 ] { t => t.f should be > 5 }
  }

  case class Test2( x: String )
  case object Test2

  class Test3( x: String )
}

