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

package com.wix.accord.combinators

import com.wix.accord.{Validator, BaseValidator}
import com.wix.accord.ViolationBuilder._

/** Provides combinators over objects implementing [[scala.math.Ordering]].
  *
  * Implementation note: All methods here should only require [[scala.math.PartialOrdering]], but then the default
  * implicits are defined in the [[scala.math.Ordering]] companion and would therefore not be imported
  * by default at the call site.
  */
trait OrderingCombinators {

  /** A validator that succeeds only for values greater than the specified bound.
    *
    * @param bound The bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 5, expected more than 10".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  case class GreaterThan[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ]( ev.gt( _, bound ), v => v -> s"$prefix $v, expected more than $bound" )

  /** A validator that succeeds only for values greater than, or equal to, the specified bound.
    *
    * @param bound The bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 5, expected 10 or more".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  case class GreaterThanOrEqual[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ]( ev.gteq( _, bound ), v => v -> s"$prefix $v, expected $bound or more" )

  /** A validator that succeeds only for values lesser than the specified bound.
    *
    * @param bound The bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 10, expected less than 10".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  case class LesserThan[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ]( ev.lt( _, bound ), v => v -> s"$prefix $v, expected less than $bound" )

  /** A validator that succeeds only for values less than, or equal to, the specified bound.
    *
    * @param bound The bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 10, expected 5 or less".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  case class LesserThanOrEqual[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ]( ev.lteq( _, bound ), v => v -> s"$prefix $v, expected $bound or less" )

  /** A validator that succeeds only for value equivalent (as determined by [[scala.math.Ordering.equiv]])
    * to the specified bound.
    *
    * @param other The fixed value against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 10, expected 5".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  case class EquivalentTo[ T ]( other: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ]( ev.equiv( _, other ), v => v -> s"$prefix $v, expected $other" )


  /** A base trait for a validator that succeeds only for values between the specified bounds, and may be
    * inclusive ''or'' exclusive.
    *
    * @tparam T The object type this validator operates on.
    */
  sealed trait InRange[ T ] extends Validator[ T ] {
    protected def lowerBound: T
    protected def upperBound: T
    def isExclusive: Boolean

    /** Returns an upper-bound-exclusive version of this validator. */
    def exclusive: InRangeExclusive[ T ]
    /** Returns an upper-bound-inclusive version of this validator. */
    def inclusive: InRangeInclusive[ T ]
  }

  /** A validator that succeeds only for values between the specified bounds (both bounds are inclusive). The
    * [[com.wix.accord.combinators.OrderingCombinators.InRange.exclusive]] method can be used to derive a
    * validator that excludes the upper bound.
    *
    * @param lowerBound The lower bound against which values are validated.
    * @param upperBound The lower bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 10, expected between 5 and 7".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  case class InRangeInclusive[ T ]( lowerBound: T, upperBound: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ](
      v => ev.gteq( v, lowerBound ) && ev.lteq( v, upperBound ),
      v => v -> s"$prefix $v, expected between $lowerBound and $upperBound" )
    with InRange[ T ] {

    def isExclusive = false
    def exclusive = InRangeExclusive( lowerBound, upperBound, prefix )
    def inclusive = this
  }

  /** A validator that succeeds only for values between the specified bounds (exclusive of the upper bound).
    * The [[com.wix.accord.combinators.OrderingCombinators.InRange.inclusive]] method can be used to derive a
    * validator that includes the upper bound.
    *
    * @param lowerBound The lower bound against which values are validated.
    * @param upperBound The lower bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 10, expected between 5 and 7 (exclusively)".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  case class InRangeExclusive[ T ]( lowerBound: T, upperBound: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ](
      v => ev.gteq( v, lowerBound ) && ev.lt( v, upperBound ),
      v => v -> s"$prefix $v, expected between $lowerBound and $upperBound (exclusively)" )
    with InRange[ T ] {

    def isExclusive = true
    def exclusive = this
    def inclusive = InRangeInclusive( lowerBound, upperBound, prefix )
  }
}
