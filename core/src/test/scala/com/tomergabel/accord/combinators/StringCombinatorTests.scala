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

package com.tomergabel.accord.combinators

import org.scalatest.{Matchers, WordSpec}
import com.tomergabel.accord.ResultMatchers

class StringCombinatorTests extends CombinatorTestSpec {

  "StartsWith combinator" should {
    "successfully validate a string that starts with the specified prefix" in {
      val left = "ham and eggs"
      val validator = new StartsWith( "ham" )
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = "eggs and ham"
      val validator = new StartsWith( "ham" )
      validator( left ) should failWith( testContext -> "must start with 'ham'" )
    }
  }
}
