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

package com.wix.accord.scalatest

import com.wix.accord.Descriptions.{Generic, Path}
import org.scalatest.{Matchers, WordSpec}
import com.wix.accord._
import com.wix.accord.GroupViolation
import com.wix.accord.RuleViolation
import com.wix.accord.Failure

class ResultMatchersTest extends WordSpec with Matchers with ResultMatchers {

  "RuleViolationMatcher" should {

    val sampleViolation = RuleViolation( "value", "constraint", Path( Generic( "description" ) ) )

    "correctly match a rule violation based on value" in {
      val matchRule = RuleViolationMatcher( value = "value" )
      sampleViolation should matchRule
    }

    "correctly match a rule violation based on constraint" in {
      val matchRule = RuleViolationMatcher( constraint = "constraint" )
      sampleViolation should matchRule
    }

    "correctly match a rule violation based on description" in {
      val matchRule = RuleViolationMatcher( path = Path( Generic( "description" ) ) )
      sampleViolation should matchRule
    }

    "correctly match a rule violation based on legacy description" in {
      val matchRule = RuleViolationMatcher( legacyDescription = "description" )
      sampleViolation should matchRule
    }

    "fail to match a non-matching rule violation" in {
      val matchRule = RuleViolationMatcher( value = "incorrect" )
      sampleViolation should not( matchRule )
    }
  }

  "GroupViolationMatcher" should {

    val sampleRule = RuleViolation( "value", "constraint", Path( Generic( "description" ) ) )
    val sampleGroup = GroupViolation( "group", "violation", Set( sampleRule ), Path( Generic( "ftw" ) ) )

    "correctly match a group violation based on value" in {
      val matchRule = GroupViolationMatcher( value = "group" )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on constraint" in {
      val matchRule = GroupViolationMatcher( constraint = "violation" )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on description" in {
      val matchRule = GroupViolationMatcher( path = Path( Generic( "ftw" ) ) )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on legacy description" in {
      val matchRule = GroupViolationMatcher( legacyDescription = "ftw" )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on children" in {
      val matchChildRule =
        RuleViolationMatcher( value = "value", constraint = "constraint", path = Path( Generic( "description" ) ) )
      val matchRule = GroupViolationMatcher( violations = Set( matchChildRule ) )
      sampleGroup should matchRule
    }

    "fail to match a non-matching group violation due to a constraint on a group property" in {
      val matchRule = GroupViolationMatcher( value = "incorrect" )
      sampleGroup should not( matchRule )
    }

    "fail to match a non-matching group violation due to a nonmatching child rule" in {
      val nonmatchingChildRule = RuleViolationMatcher( value = "incorrect" )
      val matchRule = GroupViolationMatcher( violations = Set( nonmatchingChildRule ) )
      sampleGroup should not( matchRule )
    }
  }

  "Matcher construction DSL" should {

    "generate a correct rule violation for a Tuple2[String, String] (deprecated)" in {
      val rv: RuleViolationMatcher = "description" -> "constraint"
      rv.legacyDescription shouldEqual "description"
      rv.path should ===( null )
      rv.constraint shouldEqual "constraint"
      rv.value should ===( null )
    }

    "generate a correct group violation via group() with legacy description (deprecated)" in {
      val gv = group( "description", "constraint", "description" -> "constraint" )
      gv.legacyDescription shouldEqual "description"
      gv.path should ===( null )
      gv.constraint shouldEqual "constraint"
      gv.value should ===( null )
      gv.violations should contain only
        RuleViolationMatcher( legacyDescription = "description", constraint = "constraint" )
    }

    "generate a correct rule violation for a Tuple2[Description, String]" in {
      val rv: RuleViolationMatcher = Generic( "description" ) -> "constraint"
      rv.legacyDescription should ===( null )
      rv.path shouldEqual Path( Generic( "description" ) )
      rv.constraint shouldEqual "constraint"
      rv.value should ===( null )
    }

    "generate a correct group violation via group()" in {
      val gv = group( Generic( "description" ), "constraint", Path( Generic( "description" ) ) -> "constraint" )
      gv.legacyDescription should ===( null )
      gv.path shouldEqual Path( Generic( "description" ) )
      gv.constraint shouldEqual "constraint"
      gv.value should ===( null )
      gv.violations should contain only
        RuleViolationMatcher( path = Path( Generic( "description" ) ), constraint = "constraint" )
    }
  }

  "failWith matcher" should {

    val result: Result = Failure( Set( RuleViolation( "value", "constraint", Path( Generic( "description" ) ) ) ) )

    "succeed if a validation rule matches successfully" in {
      result should failWith( Path( Generic( "description" ) ) -> "constraint" )
    }

    "fail if a validation rule does not match" in {
      result should not( failWith( Path( Generic( "invalid" ) ) -> "invalid" ) )
    }
  }

  "aSuccess matcher" should {

    "succeed if matching a Success" in {
      Success should be( aSuccess )
    }

    "fail if matching a Failure" in {
      Failure( Set.empty ) shouldNot be( aSuccess )
    }
  }

  "aFailure matcher" should {

    "fail if matching a Success" in {
      Success shouldNot be( aFailure )
    }

    "succeed if matching a Failure" in {
      Failure( Set.empty ) should be( aFailure )
    }
  }
}
