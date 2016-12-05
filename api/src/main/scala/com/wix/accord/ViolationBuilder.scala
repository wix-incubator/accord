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

/** Provides a convenience DSL for generating violations:
  *
  * - Rule violations can be created by specifying a value and constraint message as a tuple, for example:
  *   `v -> "must not be empty"`
  * - Group violations can be created by extending the above to include children, as in:
  *   `v -> "does not match any of the rules" -> Seq( v.firstName -> "first name must be empty", ... )`
  */
trait ViolationBuilder {
  import scala.language.implicitConversions

  /** Converts a tuple of the form value->constraint to a [[com.wix.accord.RuleViolation]]. */
  implicit def ruleViolationFromTuple( v: ( Any, String ) ): RuleViolation =
    RuleViolation( value = v._1, constraint = v._2 )

  /** Converts an extended tuple of the form value->constraint->ruleSeq to a [[com.wix.accord.GroupViolation]]. */
  implicit def groupViolationFromTuple( v: ( ( Any, String ), Set[ Violation ] ) ): GroupViolation =
    GroupViolation( value = v._1._1, constraint = v._1._2, children = v._2 )

  /** Wraps a single violation to a [[com.wix.accord.Failure]]. */
  implicit def singleViolationToFailure[ V ]( v: V )( implicit ev: V => Violation ): Failure =
   Failure( Set( v ) )
}

object ViolationBuilder extends ViolationBuilder {
  /** A convenience method that takes a predicate and a violation generator, evaluates the predicate and constructs
    * the appropriate [[com.wix.accord.Result]].
    *
    * @param test The predicate to be evaluated.
    * @param violation A violation generator; only gets executed if the test fails.
    * @return [[com.wix.accord.Success]] if the predicate evaluated successfully, or a [[com.wix.accord.Failure]]
    *        with the generated violation otherwise.
    */
  def result( test: => Boolean, violation: => Violation ): Result =
    if ( test ) Success else Failure( Set( violation ) )
}
