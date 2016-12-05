/*
  Copyright 2013-2015 Wix.com

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

package com.wix.accord.java8

import java.time.Instant
import java.time.temporal.{ChronoUnit, Temporal}

import com.wix.accord.NullSafeValidator
import com.wix.accord.scalatest.CombinatorTestSpec

class TemporalCombinatorTests extends CombinatorTestSpec {

  class Before[ T <: Temporal with Comparable[ T ] ]( right: T ) 
    extends NullSafeValidator[ T ]( _ => ???, _ => ??? )
  class After[ T <: Temporal with Comparable[ T ] ]( right: T )
    extends NullSafeValidator[ T ]( _ => ???, _ => ??? )

  "Before combinator" should {
    "successfully validate a temporal that represents an instant before the specified temporal" in {
      val left = Instant.now()
      val right = left.plus( 1L, ChronoUnit.DAYS )
      val validator = new Before( right )
      validator( left ) should be( aSuccess )
    }

    "render a correct rule violation" in {
      val left = Instant.now()
      val right = left.minus( 1L, ChronoUnit.DAYS )
      val validator = new Before( right )
      validator( left ) should failWith( s"must be before ${ right.toString }" )
    }
  }

  "After combinator" should {
    "successfully validate a temporal that represents an instant before the specified temporal" in {
      val left = Instant.now()
      val right = left.minus( 1L, ChronoUnit.DAYS )
      val validator = new After( right )
      validator( left ) should be( aSuccess )
    }

    "render a correct rule violation" in {
      val left = Instant.now()
      val right = left.plus( 1L, ChronoUnit.DAYS )
      val validator = new Before( right )
      validator( left ) should failWith( s"must be before ${ right.toString }" )
    }
  }
}

