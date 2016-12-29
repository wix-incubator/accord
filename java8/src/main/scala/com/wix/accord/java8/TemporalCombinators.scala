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

package com.wix.accord.java8

import java.time.Duration
import java.time.temporal.{Temporal, TemporalUnit}

import com.wix.accord.{NullSafeValidator, Result, Validator}
import com.wix.accord.ViolationBuilder._

/** Combinators that operate specifically on [[java.time.temporal.Temporal temporals]] (and subclasses thereof). */
trait TemporalCombinators {

  /** Implements [[scala.math.Ordering Ordering]] over [[java.time.temporal.Temporal Temporal]] and its
    * subclasses.
    *
    * Implementation note: this assumes `T` implements [[java.lang.Comparable Comparable]], which per
    * the JavaDoc for [[java.time.temporal.Temporal]] should always be the case.
    *
    * @tparam T The specific temporal type for which to construct an [[scala.math.Ordering Ordering]].
    */
  implicit def temporalOrdering[ T <: Temporal ]: Ordering[ T ] =
    new Ordering[ T ] { override def compare( x: T, y: T ): Int = x.asInstanceOf[ Comparable[ T ] ].compareTo( y ) }

  // Convenience extension
  private implicit class TemporalComparison[ T <: Temporal ]( left: T ) {
    def compareTo( right: T ): Int = left.asInstanceOf[ Comparable[ T ] ].compareTo( right )
  }

  /** A validator that succeeds only for values that come strictly before the specified bound.
    *
    * @param bound The bound against which values are validated.
    * @tparam T The specific temporal type this validator operates on.
    */
  class Before[ T <: Temporal ]( right: T )
    extends NullSafeValidator[ T ]( _.compareTo( right ) < 0, _ -> s"must be before $right" )

  /** A validator that succeeds only for values that come strictly after before the specified bound.
    *
    * @param bound The bound against which values are validated.
    * @tparam T The specific temporal type this validator operates on.
    */
  class After[ T <: Temporal ]( right: T )
    extends NullSafeValidator[ T ]( _.compareTo( right ) > 0, _ -> s"must be after $right" )

  /** A validator that succeeds only for values that are within (i.e. before or after) a duration of the specified
    * temporal (for example, "within a month of this person's birth date").
    *
    * This is essentially equivalent to `(value >= of - duration) && (value <= of + duration)`.
    *
    * @param of The desired temporal.
    * @param duration The allowed tolerance.
    * @param friendlyDuration A textual representation of the specified duration ([[java.time.Duration durations]]
    *                         are rendered to ISO-8601 representations by default, which is likely not the desirable
    *                         outcome).
    * @tparam T The specific temporal type this validator operates on.
    */
  class Within[ T <: Temporal ]( of: T, duration: Duration, friendlyDuration: => String )
    extends NullSafeValidator[ T ](
      t => of.minus( duration ).compareTo( t ) <= 0 && of.plus( duration ).compareTo( t ) >= 0,
      _ -> s"must be within $friendlyDuration of $of" )
}
