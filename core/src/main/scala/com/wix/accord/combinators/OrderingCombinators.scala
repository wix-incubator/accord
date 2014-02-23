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

import com.wix.accord.{BaseValidator, Validator}

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
  class GreaterThan[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] ) extends BaseValidator[ T ] {
    def apply( value: T ) =
      result( ev.gt( value, bound ), value -> s"$prefix $value, expected more than $bound" )
  }

  /** A validator that succeeds only for values greater than, or equal to, the specified bound.
    *
    * @param bound The bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 5, expected 10 or more".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  class GreaterThanOrEqual[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ] {
    
    def apply( value: T ) =
      result( ev.gteq( value, bound ), value -> s"$prefix $value, expected $bound or more" )
  }

  /** A validator that succeeds only for values lesser than the specified bound.
    *
    * @param bound The bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 10, expected less than 10".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  class LesserThan[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] ) extends BaseValidator[ T ] {
    def apply( value: T ) =
      result( ev.lt( value, bound ), value -> s"$prefix $value, expected less than $bound" )
  }

  /** A validator that succeeds only for values less than, or equal to, the specified bound.
    *
    * @param bound The bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 10, expected 5 or less".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  class LesserThanOrEqual[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ] {
    
    def apply( value: T ) =
      result( ev.lteq( value, bound ), value -> s"$prefix $value, expected $bound or less" )
  }

  /** A validator that succeeds only for value equivalent (as determined by [[scala.math.Ordering.equiv]])
    * to the specified bound.
    *
    * @param other The fixed value against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 10, expected 5".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  class EquivalentTo[ T ]( other: T, prefix: String )( implicit ev: Ordering[ T ] ) extends BaseValidator[ T ] {
    def apply( value: T ) =
      result( ev.equiv( value, other ), value -> s"$prefix $value, expected $other" )
  }

  /** A validator that succeeds only for values between the specified bounds (both bounds are inclusive). The
    * [[com.wix.accord.combinators.OrderingCombinators.Between.exclusive]] method can be used to derive a
    * validator that excludes the upper bound.
    *
    * @param lowerBound The lower bound against which values are validated.
    * @param upperBound The lower bound against which values are validated.
    * @param prefix A prefix for violation messages; for example, specifying `"got"` will result in a
    *               constraint violation like "got 10, expected between 5 and 7".
    * @param ev Evidence that `T` is ordered (i.e. a [[scala.math.Ordering]] of `T` is available).
    * @tparam T The object type this validator operates on.
    */
  class Between[ T ]( lowerBound: T, upperBound: T, prefix: String )( implicit ev: Ordering[ T ] )
    extends BaseValidator[ T ]{
    
    def apply( x: T ) =
      result(
        ev.gteq( x, lowerBound ) && ev.lteq( x, upperBound ),
        x -> s"$prefix $x, expected between $lowerBound and $upperBound" )

    /** Returns a new validator based on the provided bounds and prefix, but which treats the upper bound
      * as exclusive. The resulting constraint violation will consequently look similar to
      * "got 10, expected between 5 and 7 (exclusively)".
      */
    def exclusive = new Validator[ T ] {
      def apply( x: T ) =
        result(
          ev.gteq( x, lowerBound ) && ev.lt( x, upperBound ),
          x -> s"$prefix $x, expected between $lowerBound and $upperBound (exclusively)" )
    }
  }
}
