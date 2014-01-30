/*
  Copyright 2013 Wix.com

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

import com.wix.accord.{RuleViolation, Validator}
import com.wix.accord.combinators._
import scala.collection.immutable.NumericRange

/**
 * A useful helper class when validating numerical properties (size, length, arity...). Provides the usual
 * arithmetic operators in a consistent manner across all numerical property combiners. Violation messages
 * are structured based on the provided snippet and the property/constraint values, for example:
 *
 * ```
 * class StringLength extends NumericPropertyWrapper[ String, Int, String ]( _.length, "has length" )
 *
 * val result = ( new StringLegnth < 16 ) apply "This text is too long"
 * // result is a Failure; violation text: "has length 21, expected 16 or less".
 * ```
 *
 * Additionally provides a representation type `Repr` to shortcut type inference; for an expression like
 * `c.students has size > 0`, the object under validation is `c.students` but its inferred type isn't resolved
 * until the leaf node of the expression tree (in this case, the method call `> 0`). That means all constraints,
 * view bounds and the like have to exist at the leaf node, or in other words on the method call itself; to
 * generalize this, each arithmetic method requires an implicit `T => Repr` conversion, and because `Repr`
 * is specified by whomever instantiates [[OrderedPropertyWrapper]] the
 * view bound is placed correctly at the call site. (*whew*, hope that made sense)
 *
 * @param extractor A function which extracts the property value from the representation of the object under
 *                  validation (e.g. `( p: String ) => p.length`)
 * @param snippet A textual snippet describing what the validator does (e.g. `has length`)
 * @tparam T The type of the object under validation
 * @tparam P The type of the property under validation, must be numeric
 * @tparam Repr The runtime representation of the object under validation. When it differs from `T`, an implicit
 *              conversion `T => Repr` is required ''at the call site''.
 */
abstract class OrderedPropertyWrapper[ T, P : Numeric, Repr ]( extractor: Repr => P, snippet: String ) {
  // TODO generalize so this can be implemented based on OrderingOps.
  // It's unclear whether or not this is even possible because of the extra required conversion step (T => Repr).

  /** Generates a validator that succeeds only if the property value is greater than the specified bound. */
  def >( other: P )( implicit repr: T => Repr ) = new GreaterThan( other, snippet ) compose ( repr andThen extractor )

  /** Generates a validator that succeeds only if the property value is less than the specified bound. */
  def <( other: P )( implicit repr: T => Repr ) = new LesserThan( other, snippet ) compose ( repr andThen extractor )

  /** Generates a validator that succeeds if the property value is greater than or equal to the specified bound. */
  def >=( other: P )( implicit repr: T => Repr ) =
    new GreaterThanOrEqual( other, snippet ) compose ( repr andThen extractor )

  /** Generates a validator that succeeds if the property value is less than or equal to the specified bound. */
  def <=( other: P )( implicit repr: T => Repr ) =
    new LesserThanOrEqual( other, snippet ) compose ( repr andThen extractor )

  /** Generates a validator that succeeds if the property value is exactly equal to the specified value. */
  def ==( other: P )( implicit repr: T => Repr ) = new EquivalentTo( other, snippet ) compose ( repr andThen extractor )
}

/** Provides combinators over objects implementing [[scala.math.Ordering]].
  *
  * Implementation note: All methods here should only require [[scala.math.PartialOrdering]], but then the default
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
