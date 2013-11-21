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

class CollectionCombinatorTests extends CombinatorTestSpec {

  "Size combinator" should {
    case class Test( size: Int )

    "successfully validate a greater-than rule" in {
      val left = Test( 10 )
      val validator = new Size[ Test ] > 5
      validator( left ) should be( aSuccess )
    }
    "render a correct greater-than rule violation" in {
      val left = Test( 0 )
      val validator = new Size[ Test ] > 5
      validator( left ) should failRule( testContext -> "has size 0, expected more than 5" )
    }
    "successfully validate a greater-than-or-equal rule" in {
      val left = Test( 10 )
      val validator = new Size[ Test ] >= 10
      validator( left ) should be( aSuccess )
    }
    "render a correct greater-than-or-equal rule violation" in {
      val left = Test( 0 )
      val validator = new Size[ Test ] >= 5
      validator( left ) should failRule( testContext -> "has size 0, expected 5 or more" )
    }
    "successfully validate a lesser-than rule" in {
      val left = Test( 5 )
      val validator = new Size[ Test ] < 10
      validator( left ) should be( aSuccess )
    }
    "render a correct lesser-than rule violation" in {
      val left = Test( 10 )
      val validator = new Size[ Test ] < 10
      validator( left ) should failRule( testContext -> "has size 10, expected less than 10" )
    }
    "successfully validate a lesser-than-or-equal rule" in {
      val left = Test( 10 )
      val validator = new Size[ Test ] <= 10
      validator( left ) should be( aSuccess )
    }
    "render a correct lesser-than-or-equal rule violation" in {
      val left = Test( 10 )
      val validator = new Size[ Test ] <= 5
      validator( left ) should failRule( testContext -> "has size 10, expected 5 or less" )
    }
  }

  "Empty combinator" should {
    "successfully validate an empty option" in {
      val left = None
      val validator = new Empty[ Option[ String ] ]
      validator( left ) should be( aSuccess )
    }
    "successfully validate an empty string" in {
      val left = ""
      val validator = new Empty[ String ]
      validator( left ) should be( aSuccess )
    }
    "successfully validate an empty sequence" in {
      val left = Seq.empty[ String ]
      val validator = new Empty[ Seq[ String ] ]
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = Some( "content" )
      val validator = new Empty[ Option[ String ] ]
      validator( left ) should failRule( testContext -> "must be empty" )
    }
  }

  "NotEmpty combinator" should {
    "successfully validate a non-empty option" in {
      val left = Some( "content" )
      val validator = new NotEmpty[ Option[ String ] ]
      validator( left ) should be( aSuccess )
    }
    "successfully validate a non-empty string" in {
      val left = "content"
      val validator = new NotEmpty[ String ]
      validator( left ) should be( aSuccess )
    }
    "successfully validate a non-empty sequence" in {
      val left = Seq( "content" )
      val validator = new NotEmpty[ Seq[ String ] ]
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = None
      val validator = new NotEmpty[ Option[ String ] ]
      validator( left ) should failRule( testContext -> "must not be empty" )
    }
  }
}
