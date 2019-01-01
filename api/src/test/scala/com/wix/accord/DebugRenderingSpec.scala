/*
  Copyright 2013-2019 Wix.com

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

import com.wix.accord.Descriptions.{Generic, Path}
import org.scalatest.{Matchers, WordSpec}

/**
  * Created by grenville on 9/6/16.
  */
class DebugRenderingSpec extends WordSpec with Matchers {

  object RuleViolations {
    val full = RuleViolation( "string", "must start with \"test\"", Path( Generic( "full" ) ) )
    val nullValue = RuleViolation( null, "is a null", Path( Generic( "nullValue" ) ) )
    val emptyString = RuleViolation( "", "must not be empty", Path( Generic( "emptyString" ) ) )
    val emptySeq = RuleViolation( Seq.empty[ Int ], "must not be empty", Path( Generic( "emptySeq" ) ) )
  }

  object GroupViolations {
    val children = Set[ Violation ]( RuleViolations.full, RuleViolations.emptySeq )

    val full =
      GroupViolation( "some value", "doesn't meet any of the requirements", children, Path( Generic( "full" ) ) )
    val nullValue =
      GroupViolation( null, "doesn't meet any of the requirements", Set.empty, Path( Generic( "nullValue" ) ) )
    val noChildren =
      GroupViolation( "some value", "doesn't meet any of the requirements", Set.empty, Path( Generic( "noChildren" ) ) )
    val singleChild =
      GroupViolation( "some value", "doesn't meet any of the requirements", Set( RuleViolations.full ), Path( Generic( "singleChild" ) ) )
    val nested =
      GroupViolation( "some value", "doesn't meet any of the requirements", children + full, Path( Generic( "nested" ) ) )
    val tieBreaker =
      GroupViolation( "some value", "doesn't meet any of the requirements", Set( RuleViolations.nullValue, this.nullValue ), Path( Generic( "tieBreaker" ) ) )
  }

  "RuleViolation debug rendering" should {
    import RuleViolations._

    "correctly render a violation" in {
      full.toString shouldEqual """full with value "string" must start with "test""""
    }
    "elide null values" in {
      nullValue.toString shouldEqual "nullValue is a null"
    }
    "elide empty collections" in {
      emptySeq.toString shouldEqual "emptySeq must not be empty"
    }
    "elide empty strings" in {
      emptyString.toString shouldEqual "emptyString must not be empty"
    }
  }

  "GroupViolation debug rendering" should {
    import GroupViolations._

    "correctly handle a group with no children" in {
      noChildren.toString shouldEqual """noChildren with value "some value" doesn't meet any of the requirements"""
    }
    "nest children if available" in {
      singleChild.toString shouldEqual
        """
          |singleChild with value "some value" doesn't meet any of the requirements:
          |`-- full with value "string" must start with "test"
        """.stripMargin.trim
    }
    "order rule violations first" in {
      tieBreaker.toString shouldEqual
        """
          |tieBreaker with value "some value" doesn't meet any of the requirements:
          ||-- nullValue is a null
          |`-- nullValue doesn't meet any of the requirements
        """.stripMargin.trim
    }
    "order children lexicographically by description" in {
      full.toString shouldEqual
        """
          |full with value "some value" doesn't meet any of the requirements:
          ||-- emptySeq must not be empty
          |`-- full with value "string" must start with "test"
        """.stripMargin.trim
    }
    "handle multiple levels of nesting" in {
      nested.toString shouldEqual
        """
          |nested with value "some value" doesn't meet any of the requirements:
          ||-- emptySeq must not be empty
          ||-- full with value "string" must start with "test"
          |`-- full with value "some value" doesn't meet any of the requirements:
          |    |-- emptySeq must not be empty
          |    `-- full with value "string" must start with "test"
        """.stripMargin.trim
    }
    "elide null values" in {
      nullValue.toString shouldEqual "nullValue doesn't meet any of the requirements"
    }
  }
}
