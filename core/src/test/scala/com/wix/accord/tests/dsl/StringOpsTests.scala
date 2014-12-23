/*
  Copyright 2013-2014 Wix.com

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

package com.wix.accord.tests.dsl

import com.wix.accord.TestDomain._
import com.wix.accord.TestDomainMatchers
import org.scalatest.{Matchers, WordSpec}

class StringOpsTests extends WordSpec with TestDomainMatchers with Matchers {
  import com.wix.accord.tests.dsl.StringOpsTests._

  // Quick and dirty helper to make the tests a little more terse
  implicit class EnrichString( text: String ) {
    def validatedWith( validator: Validator[ Test ] ) = validator apply Test( text )
  }

  "startWith" should {
    "successfully validate a correct string" in {
      "testing" validatedWith startsWithValidator should be( aSuccess )
    }
    "successfully validate an incorrect string" in {
      "some string" validatedWith startsWithValidator should be( aFailure )
    }
  }

  "endWith" should {
    "successfully validate a correct string" in {
      "some test" validatedWith endsWithValidator should be( aSuccess )
    }
    "successfully validate an incorrect string" in {
      "some string" validatedWith endsWithValidator should be( aFailure )
    }
  }
  
  "matchRegex" should {
    "successfully validate a correct string based on a text pattern" in {
      "tests galore" validatedWith matchRegexValidator should be( aSuccess )
    }
    "successfully validate an incorrect string based on a text pattern" in {
      "some string" validatedWith matchRegexValidator should be( aFailure )
    }
    "successfully validate a correct string based on a Scala regex" in {
      "tests galore" validatedWith matchRegexByScalaValidator should be( aSuccess )
    }
    "successfully validate an incorrect string based on a Scala regex" in {
      "some string" validatedWith matchRegexByScalaValidator should be( aFailure )
    }
    "successfully validate a correct string based on a java.util.regex.Pattern" in {
      "tests galore" validatedWith matchRegexByPatternValidator should be( aSuccess )
    }
    "successfully validate an incorrect string based on a java.util.regex.Pattern" in {
      "some string" validatedWith matchRegexByPatternValidator should be( aFailure )
    }
  }

  "matchRegexFully" should {
    "successfully validate a correct string based on a text pattern" in {
      "so many tests" validatedWith matchRegexFullyValidator should be( aSuccess )
    }
    "successfully validate an incorrect string based on a text pattern" in {
      "tests galore" validatedWith matchRegexFullyValidator should be( aFailure )
    }
    "successfully validate a correct string based on a Scala regex" in {
      "so many tests" validatedWith matchRegexFullyByScalaValidator should be( aSuccess )
    }
    "successfully validate an incorrect string based on a Scala regex" in {
      "tests galore" validatedWith matchRegexFullyByScalaValidator should be( aFailure )
    }
    "successfully validate a correct string based on a java.util.regex.Pattern" in {
      "so many tests" validatedWith matchRegexFullyByPatternValidator should be( aSuccess )
    }
    "successfully validate an incorrect string based on a java.util.regex.Pattern" in {
      "tests galore" validatedWith matchRegexFullyByPatternValidator should be( aFailure )
    }
  }
}

object StringOpsTests {
  case class Test( s: String )

  val               startsWithValidator = validator[ Test ] { _.s should startWith( "test" ) }
  val                 endsWithValidator = validator[ Test ] { _.s should endWith( "test" ) }
  val               matchRegexValidator = validator[ Test ] { _.s should matchRegex( "test(s?)" ) }
  val        matchRegexByScalaValidator = validator[ Test ] { _.s should matchRegex( "test(s?)".r ) }
  val      matchRegexByPatternValidator = validator[ Test ] { _.s should matchRegex( "test(s?)".r.pattern ) }
  val          matchRegexFullyValidator = validator[ Test ] { _.s should matchRegexFully( ".*test(s?)" ) }
  val   matchRegexFullyByScalaValidator = validator[ Test ] { _.s should matchRegexFully( ".*test(s?)".r ) }
  val matchRegexFullyByPatternValidator = validator[ Test ] { _.s should matchRegexFully( ".*test(s?)".r.pattern ) }
}

