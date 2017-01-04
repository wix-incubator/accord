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

package com.wix.accord

import com.wix.accord.Descriptions.Generic
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by grenville on 9/6/16.
  */
class DebugRenderingSpec extends FlatSpec with Matchers {

  val subSampleRule = RuleViolation( "value", "is null", Generic( "subSampleRule" ) )
  val subSampleGroup = GroupViolation( "subgroup", "is not valid", Set( subSampleRule ), Generic( "subgroup" ) )
  val sampleRule1 = RuleViolation( "value", "is null", Generic( "sampleRule1" ) )
  val sampleRule2 = RuleViolation( "value", "is null", Generic( "sampleRule2" ) )
  val sampleGroup =
    GroupViolation( "group", "is not valid", Set( sampleRule1, sampleRule2, subSampleGroup ), Generic( "request" ) )

  "Debug rendering for violations" should "correctly handle a RuleViolation" in {
    sampleRule1.toString shouldEqual "sampleRule1 is null"
  }

  it should "correctly handle a GroupViolation" in {
    sampleGroup.toString shouldEqual "request is not valid: Set(sampleRule1 is null, sampleRule2 is null, " +
      "subgroup is not valid: Set(subSampleRule is null))"
  }
}
