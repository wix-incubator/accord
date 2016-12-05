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

import java.time.{LocalDateTime, ZoneOffset}
import java.time.temporal.{ChronoUnit, Temporal}

import com.wix.accord.scalatest.CombinatorTestSpec

class TemporalCombinatorTests extends CombinatorTestSpec {

  "Before combinator" should {
    "successfully validate a temporal that represents an instant before the specified temporal" in {
      val left = LocalDateTime.now()
      val right = left.plus( 1L, ChronoUnit.DAYS )
      val validator = new Before( right )
      validator( left ) should be( aSuccess )
    }

    "render a correct rule violation" in {
      val left = LocalDateTime.now()
      val right = left.minus( 1L, ChronoUnit.DAYS )
      val validator = new Before( right )
      validator( left ) should failWith( s"must be before ${ right.toString }" )
    }
  }

  "After combinator" should {
    "successfully validate a temporal that represents an instant before the specified temporal" in {
      val left = LocalDateTime.now()
      val right = left.minus( 1L, ChronoUnit.DAYS )
      val validator = new After( right )
      validator( left ) should be( aSuccess )
    }

    "render a correct rule violation" in {
      val left = LocalDateTime.now()
      val right = left.plus( 1L, ChronoUnit.DAYS )
      val validator = new After( right )
      validator( left ) should failWith( s"must be after ${ right.toString }" )
    }
  }

//  "Within combinator" should {
//    "successfully validate a temporal that represents an instant within tolerance of the specified temporal" in {
//      val left = LocalDateTime.now()
//      val validator = new Within( right, 1L, ChronoUnit.DAYS )
//      validator( left ) should be( aSuccess )
//    }
//  }

  "Default combinator library" should {
    import TemporalCombinatorTests._

    "handle temporal equality via generic equality" in {
      val otherEpoch = LocalDateTime.ofEpochSecond( 0L, 0, ZoneOffset.UTC )
      atEpoch( otherEpoch ) should be( aSuccess )
      atEpoch( now ) should be( aFailure )
    }

    "handle temporal inequality via generic equality" in {
      notAtEpoch( epoch ) should be( aFailure )
      notAtEpoch( now ) should be( aSuccess )
    }

    "handle temporal comparison via OrderingOps" in {
      val lastWeek = now.minus( 1L, ChronoUnit.WEEKS )
      val nextWeek = now.plus( 1L, ChronoUnit.WEEKS )
      duringLastYear( epoch ) should be( aFailure )
      duringLastYear( lastWeek ) should be( aSuccess )
      duringLastYear( nextWeek ) should be( aFailure )
    }
  }
}

object TemporalCombinatorTests {
  // Support validators
  val epoch: Temporal = LocalDateTime.parse( "1970-01-01T00:00:00" )
  val now: Temporal = LocalDateTime.now()
  val lastYear: Temporal = now.minus( 1L, ChronoUnit.YEARS )

  import com.wix.accord.dsl._

  val atEpoch = validator[ Temporal ] { t => t is equalTo( epoch ) }
  val notAtEpoch = validator[ Temporal ] { t => t is notEqualTo( epoch ) }
  val duringLastYear = validator[ Temporal ] { t => t is between( lastYear, now ) }
}

