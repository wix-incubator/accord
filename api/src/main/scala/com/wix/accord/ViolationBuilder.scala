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
  implicit def ruleViolationFromTuple( v: ( Any, String ) ) =
    RuleViolation( value = v._1, constraint = v._2, description = None )

  /** Converts an extended tuple of the form value->constraint->ruleSeq to a [[com.wix.accord.GroupViolation]]. */
  implicit def groupViolationFromTuple( v: ( ( Any, String ), Seq[ Violation ] ) ) =
    GroupViolation( value = v._1._1, constraint = v._1._2, description = None, children = v._2 )

  implicit def singleViolationToFailure[ V <% Violation ]( v: V ): Failure = Failure( Seq( v ) )
}

object ViolationBuilder extends ViolationBuilder {
  def result( test: => Boolean, violation: => Violation ) =
    if ( test ) Success else Failure( Seq( violation ) )
}
