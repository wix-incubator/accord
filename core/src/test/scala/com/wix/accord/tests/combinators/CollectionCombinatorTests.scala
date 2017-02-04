/*
  Copyright 2013-2016 Wix.com

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

package com.wix.accord.tests.combinators

import com.wix.accord.combinators.{Distinct, Empty, In, NotEmpty}
import com.wix.accord.scalatest.CombinatorTestSpec

class CollectionCombinatorTests extends CombinatorTestSpec {

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
      validator( left ) should failWith( "must be empty" )
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
      validator( left ) should failWith( "must not be empty" )
    }
  }

  "Distinct combinator" should {
    "be null-safe" in {
      val left: Seq[ String ] = null
      val validator = Distinct
      validator( left ) should failWith( "is a null" )
    }

    "successfully validate an empty collection" in {
      val left = Seq.empty[ String ]
      val validator = Distinct
      validator( left ) should be( aSuccess )
    }

    "successfully validate a distinct set" in {
      val left = Seq( 1, 2, 3, 4, 5 )
      val validator = Distinct
      validator( left ) should be( aSuccess )
    }

    "render a correct rule violation" in {
      val left = Seq( 1, 2, 3, 3, 4, 4, 5 )
      val validator = Distinct
      validator( left ) should failWith( "is not a distinct set; duplicates: [3, 4]" )
    }
  }

  "In combinator" should {
    val validator = new In( Set( 1, 5, 9 ), "got" )

    "successfully validate a number in range" in {
      validator( 1 ) should be( aSuccess )
    }

    "render a correct rule violation" in {
      validator( 2 ) should failWith( "got 2, expected one of: [1, 5, 9]" )
    }
  }
}
