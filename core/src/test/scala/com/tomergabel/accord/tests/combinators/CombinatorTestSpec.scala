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

package com.tomergabel.accord.tests.combinators

import org.scalatest.{WordSpec, Matchers}
import com.tomergabel.accord.scalatest.ResultMatchers

trait CombinatorTestSpec extends WordSpec with Matchers with ResultMatchers {
  protected val testContext = com.tomergabel.accord.stubValidationContext
}
