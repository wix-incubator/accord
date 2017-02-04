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

package com.wix.accord.joda.tests

import com.wix.accord.scalatest.ResultMatchers
import org.joda.time._
import org.scalatest.{Matchers, WordSpec}

class ReadableInstantOpsTests extends WordSpec with Matchers with ResultMatchers {

  import com.wix.accord.joda.{Before, After, Within}
  import ReadableInstantOpsTests._

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

object ReadableInstantOpsTests {
  import com.wix.accord.dsl._
  import com.wix.accord.joda._

  val now: Instant = DateTime.now().toInstant
  val epoch: ReadableInstant = DateTime.parse( "1970-01-01T00:00:00Z" )
  val otherEpoch = new Instant( 0L )
  val lastWeek: ReadableInstant = now.minus( Duration.standardDays( 7 ) )
  val nextWeek: ReadableInstant = now.plus( Duration.standardDays( 7 ) )
  val lastYear: ReadableInstant = now.minus( Duration.standardDays( 365 ) )
  val tomorrow: ReadableInstant = now.plus( Duration.standardDays( 1 ) )

  val beforeValidator = lastWeek is before( now )
  val afterValidator = tomorrow is after( now )
  val withinDurationValidator = tomorrow is within( Duration.standardDays( 7 ) ).of( now )

  val atEpoch = validator[ ReadableInstant ] { t => t is equalTo( epoch ) }
  val notAtEpoch = validator[ ReadableInstant ] { t => t is notEqualTo( epoch ) }
  val duringLastYear = validator[ ReadableInstant ] { t => t is between( lastYear, now ) }
}
