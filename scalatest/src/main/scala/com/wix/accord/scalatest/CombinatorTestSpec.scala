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

package com.wix.accord.scalatest

import org.scalatest.{Matchers, WordSpec}

/**
  * An opinionated helper trait for combinator specifications. The recommended practice for defining new combinators
  * is test-first via this specification:
  *
  * {{{
  * class MyCombinatorSpec extends WordSpec with ResultMatchers with Matchers {
  *
  *   def isMonotonous[ T ]: Validator[ Iterable[ T ] ] = ???
  *
  *   "isMonotonous" should {
  *     "successfully validate a monotonously-increasing sequence of numbers" in {
  *       isMonotonous( Seq( 1, 2, 3, 4, 5 ) ) should be( aSuccess )
  *     }
  *
  *     "correctly render violations on a random sequence" in {
  *       isMonotonous( Seq( 5, 4, 3, 2, 1 ) ) should failWith( "is not monotonously-increasing" )
  *     }
  *   }
  *
  * }
  * }}}
  *
  */
trait CombinatorTestSpec extends WordSpec with Matchers with ResultMatchers {
  import scala.language.implicitConversions

  implicit def elevateStringToRuleViolationMatcher( s: String ): RuleViolationMatcher =
    RuleViolationMatcher( constraint = s )
}
