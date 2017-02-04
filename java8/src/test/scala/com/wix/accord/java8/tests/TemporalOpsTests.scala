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

package com.wix.accord.java8.tests


import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneOffset}
import com.wix.accord.scalatest.ResultMatchers

import org.scalatest.{Matchers, WordSpec}

class TemporalOpsTests extends WordSpec with Matchers with ResultMatchers {

  import com.wix.accord.java8.{Before, After, Within}
  import TemporalOpsTests._

  "The expression \"is before\"" should {
    "produce a Before combinator" in {
      beforeValidator shouldBe a[ Before[_] ]
    }
  }

  "The expression \"is after\"" should {
    "produce an After combinator" in {
      afterValidator shouldBe an[ After[_] ]
    }
  }

  "The expression \"is within(...).of( target )\"" should {
    "produce a Within combinator for time unit parameters" in {
      withinTimeUnitValidator shouldBe a[ Within[_] ]
    }

    "produce a Within combinator for duration parameters" in {
      withinDurationValidator shouldBe a[ Within[_] ]
    }
  }

  "Default combinator library" should {
    "handle temporal equality via generic equality" in {
      atEpoch( otherEpoch ) should be( aSuccess )
      atEpoch( now ) should be( aFailure )
    }

    "handle temporal inequality via generic equality" in {
      notAtEpoch( epoch ) should be( aFailure )
      notAtEpoch( now ) should be( aSuccess )
    }

    "handle temporal comparison via OrderingOps" in {
      duringLastYear( epoch ) should be( aFailure )
      duringLastYear( lastWeek ) should be( aSuccess )
      duringLastYear( nextWeek ) should be( aFailure )
    }
  }
}

object TemporalOpsTests {
  import java.time.{LocalDateTime, Duration}
  import java.time.temporal.{ChronoUnit, Temporal}

  import com.wix.accord.dsl._
  import com.wix.accord.java8._

  val epoch: Temporal = LocalDateTime.parse( "1970-01-01T00:00:00" )
  val otherEpoch = LocalDateTime.ofEpochSecond( 0L, 0, ZoneOffset.UTC )
  val now: Temporal = LocalDateTime.now()
  val lastWeek: Temporal = now.minus( 1L, ChronoUnit.WEEKS )
  val nextWeek = now.plus( 1L, ChronoUnit.WEEKS )
  val lastYear: Temporal = now.minus( 1L, ChronoUnit.YEARS )
  val tomorrow: Temporal = now.plus( 1L, ChronoUnit.DAYS )

  val beforeValidator = lastWeek is before( now )
  val afterValidator = tomorrow is after( now )
  val withinTimeUnitValidator = tomorrow is within( 1L, ChronoUnit.WEEKS ).of( now )
  val withinDurationValidator = tomorrow is within( Duration.ofDays( 7L ) ).of( now )

  val atEpoch = validator[ Temporal ] { t => t is equalTo( epoch ) }
  val notAtEpoch = validator[ Temporal ] { t => t is notEqualTo( epoch ) }
  val duringLastYear = validator[ Temporal ] { t => t is between( lastYear, now ) }
}
