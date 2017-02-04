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

import com.wix.accord.scalatest.CombinatorTestSpec
import org.joda.time.{DateTime, Duration}

class ReadableInstantCombinatorTests extends CombinatorTestSpec {
  import com.wix.accord.joda.{After, Before, Within}

  "Before combinator" should {
    "successfully validate a temporal that represents an instant before the specified temporal" in {
      val left = DateTime.now()
      val right = left.plus( Duration.standardDays( 1L ) )
      val validator = new Before( right )
      validator( left ) should be( aSuccess )
    }

    "render a correct rule violation" in {
      val left = DateTime.now()
      val right = left.minus( Duration.standardDays( 1L ) )
      val validator = new Before( right )
      validator( left ) should failWith( s"must be before $right" )
    }
  }

  "After combinator" should {
    "successfully validate a temporal that represents an instant before the specified temporal" in {
      val left = DateTime.now()
      val right = left.minus( Duration.standardDays( 1L ) )
      val validator = new After( right )
      validator( left ) should be( aSuccess )
    }

    "render a correct rule violation" in {
      val left = DateTime.now()
      val right = left.plus( Duration.standardDays( 1L ) )
      val validator = new After( right )
      validator( left ) should failWith( s"must be after $right" )
    }
  }

  "Within combinator" should {
    "successfully validate a temporal that represents an instant within the specified tolerance" in {
      val now = DateTime.now()
      val anHourAgo = now.minus( Duration.standardDays( 1L ) )
      val anHourHence = now.plus( Duration.standardDays( 1L ) )
      val validator = new Within( now, Duration.standardDays( 1 ), "1 days" )
      validator( anHourAgo ) should be( aSuccess )
      validator( anHourHence ) should be( aSuccess )
    }

    "render a correct rule violation" in {
      val now = DateTime.now()
      val aWeekAgo = now.minus( Duration.standardDays( 7 ) )
      val aWeekHence = now.plus( Duration.standardDays( 7 ) )
      val validator = new Within( now, Duration.standardDays( 1 ), "1 days" )
      validator( aWeekAgo ) should failWith( s"must be within 1 days of $now" )
      validator( aWeekHence ) should failWith( s"must be within 1 days of $now" )
    }
  }
}

