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

package com.wix.accord.tests.repro

import org.scalatest.{Matchers, FlatSpec}
import com.wix.accord.scalatest.ResultMatchers

/**
  * Test case for [[https://github.com/wix/accord/issues/7 issue 7]]
  */
import Issue7._
class Issue7 extends FlatSpec with Matchers with ResultMatchers {
  "nullSafeValidator" should "fail on notNull validation" in {
    nullSafeValidator apply Test( null ) shouldBe aFailure
  }
}

object Issue7 {
  import com.wix.accord.dsl._
  case class Test( s: String )

  val nullSafeValidator = validator[ Test ] { t =>
    t.s is notNull
    t.s has size < 10 // NPE if t.s is null
  }
}
