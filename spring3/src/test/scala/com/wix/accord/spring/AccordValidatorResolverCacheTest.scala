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

package com.wix.accord.spring

import com.wix.accord.Validator
import com.wix.accord.dsl._
import org.scalatest.{Matchers, WordSpec}

import scala.reflect.ClassTag

class AccordValidatorResolverCacheTest extends WordSpec with Matchers {

  import AccordValidatorResolverCacheTest._

  "AccordValidatorResolverCache" should {

    def newResolver = new CachedTestValidatorResolver

    "return None when the class has no validator defined" in {
      newResolver.lookupValidator[ Class1 ] shouldBe empty
    }

    "return the validator when it is defined" in {
      val resolver = newResolver
      resolver.setLookupResult( validator1 )
      resolver.lookupValidator[ Class1 ] shouldBe Some( validator1 )
    }

    "consistently resolve validators for different classes" in {
      val resolver = newResolver

      resolver.setLookupResult( validator1 )
      resolver.lookupValidator[ Class1 ]

      resolver.setLookupResult( validator2 )
      resolver.lookupValidator[ Class2 ]

      resolver.lookupValidator[ Class1 ] shouldBe Some( validator1 )
      resolver.lookupValidator[ Class2 ] shouldBe Some( validator2 )
    }

    "perform validator lookup for the same class only once" in {
      val resolver = newResolver
      resolver.lookupValidator[ Class1 ]
      resolver.lookupValidator[ Class1 ]
      resolver.invocationCount shouldBe 1
    }
  }
}

object AccordValidatorResolverCacheTest {

  class TestValidatorResolver extends AccordValidatorResolver {

    private var invocationCounter = 0
    private var lookupResult: Option[ Validator[_] ] = None

    override def lookupValidator[ T : ClassTag ]: Option[ Validator [ T ] ] = {
      invocationCounter += 1
      lookupResult.map { _.asInstanceOf[ Validator[ T ] ] }
    }

    def setLookupResult( result: Validator[_] ) = {
      lookupResult = Some( result )
    }

    def invocationCount = invocationCounter
  }

  class CachedTestValidatorResolver extends TestValidatorResolver with AccordValidatorResolverCache

  class Class1

  class Class2

  val validator1 = validator[ Int ] { _ should be > 1 }

  val validator2 = validator[ Int ] { _ should be > 2 }
}
