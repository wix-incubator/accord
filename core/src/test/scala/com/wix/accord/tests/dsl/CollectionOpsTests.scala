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

package com.wix.accord.tests.dsl

import com.wix.accord.scalatest.ResultMatchers
import org.scalatest.{WordSpec, Matchers}
import com.wix.accord._

class CollectionOpsTests extends WordSpec with Matchers with ResultMatchers {
  import CollectionOpsTests._

  "empty" should {
    "successfully validate a empty collection" in {
      validate( Seq.empty )( seqEmptyValidator ) should be( aSuccess )
    }
    "successfully validate a non-empty collection" in {
      validate( Seq( 1, 2, 3 ) )( seqEmptyValidator ) should be( aFailure )
    }
  }
  "notEmpty" should {
    "successfully validate a non-empty collection" in {
      validate( Seq( 1, 2, 3 ) )( seqNotEmptyValidator ) should be( aSuccess )
    }
    "successfully validate a empty collection" in {
      validate( Seq.empty )( seqNotEmptyValidator ) should be( aFailure )
    }
  }
  "size extensions" should {
    // No need to test all extensions -- these should be covered in OrderingOpsTest. We only need to test
    // one to ensure correct constraint generation.
    "generate a correctly prefixed constraint" in {
      validate( Seq.empty )( seqSizeValidator ) should
        failWith( RuleViolationMatcher( constraint = "has size 0, expected more than 0" ) )
    }
  }
  "each extension" should {
    "be null safe" in {
      validate( null )( seqEachValidator ) should be( aFailure )
    }
  }
}

object CollectionOpsTests {
  import dsl._

  val seqEmptyValidator = validator[ Seq[_] ] { _ is empty }
  val seqNotEmptyValidator = validator[ Seq[_] ] { _ is notEmpty }
  val seqSizeValidator = validator[ Seq[_] ] { _ has size > 0 }
  val seqEachValidator = validator[ Seq[ String ] ] { _.each is empty }
}
