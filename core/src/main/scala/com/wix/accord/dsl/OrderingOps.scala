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

package com.wix.accord.dsl

import com.wix.accord.Validator
import com.wix.accord.combinators._
import scala.collection.immutable.NumericRange

/** Provides a DSL for objects implementing [[scala.math.Ordering]].
  *
  * Implementation note: All methods here should only require [[scala.math.PartialOrdering]], but the canonical
  * implicits are defined in the Ordering companion and would therefore not be imported by default at the call site.
  */
trait OrderingOps {
  protected def snippet: String = "got"

  /** Generates a validator that succeeds only if the provided value is greater than the specified bound. */
  def >[ T : Ordering ]( other: T ) = new GreaterThan( other, snippet )

  /** Generates a validator that succeeds only if the provided value is less than the specified bound. */
  def <[ T : Ordering ]( other: T ) = new LesserThan( other, snippet )

  /** Generates a validator that succeeds if the provided value is greater than or equal to the specified bound. */
  def >=[ T : Ordering ]( other: T ) = new GreaterThanOrEqual( other, snippet )

  /** Generates a validator that succeeds if the provided value is less than or equal to the specified bound. */
  def <=[ T : Ordering ]( other: T ) = new LesserThanOrEqual( other, snippet )

  /** Generates a validator that succeeds if the provided value is exactly equal to the specified value. */
  def ==[ T : Ordering ]( other: T ) = new EqualTo( other, snippet )

  /** Generates a validator that succeeds if the provided value is between (inclusive) the specified bounds.
    * The method `exclusive` is provided to specify an exclusive upper bound.
    */
  def between[ T : Ordering ]( lowerBound: T, upperBound: T ): Between[ T ] = new Between( lowerBound, upperBound, snippet )

  /** Generates a validator that succeeds if the provided value is within the specified range. */
  def within( range: Range ): Validator[ Int ] = {
    val v = between( range.start, range.end )
    if ( range.isInclusive ) v else v.exclusive
  }

  /** Generates a validator that succeeds if the provided value is within the specified range. */
  def within[ T : Ordering ]( range: NumericRange[ T ] ): Validator[ T ] = {
    val v = between( range.start, range.end )
    if ( range.isInclusive ) v else v.exclusive
  }
}
