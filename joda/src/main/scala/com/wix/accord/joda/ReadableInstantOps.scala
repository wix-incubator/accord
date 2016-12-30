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

package com.wix.accord.joda

import com.wix.accord.dsl.OrderingOps
import org.joda.time.{Duration, ReadableDuration, ReadableInstant}

/** Provides a DSL for validating [[org.joda.time.ReadableInstant ReadableInstants]] (and subclasses thereof). */
trait ReadableInstantOps {
  /** Generates a validator that succeeds only if the provided value comes strictly before the specified bound. */
  def before[ T <: ReadableInstant ]( right: T ) = new Before( right )
  /** Generates a validator that succeeds only if the provided value comes strictly after the specified bound. */
  def after[ T <: ReadableInstant ]( right: T ) = new After( right )

  /** A builder to support the `within` DSL extensions. */
  class WithinBuilder[ T <: ReadableInstant ] private[ ReadableInstantOps ]( duration: ReadableDuration, friendlyDuration: => String ) {
    /** Specifies the target temporal for the comparison. */
    def of( target: T ): Within[ T ] = new Within( target, duration, friendlyDuration )
  }

  /** Extends the Accord DSL with additional `within` operations over [[org.joda.time.ReadableInstant instants]]. */
  implicit class ExtendAccordDSL( dsl: OrderingOps ) {
    /** Builds a validator that succeeds if the provided value is within the specified tolerance of a particular
      * instant. For example:
      *
      * {{{
      *   tomorrow is within( Duration.standardDays( 7L ), "7 days" ).of( now )
      * }}}
      */
    def within[ T <: ReadableInstant ]( duration: ReadableDuration, friendlyDuration: => String ): WithinBuilder[ T ] =
      new WithinBuilder[ T ]( duration, friendlyDuration )

    /** Builds a validator that succeeds if the provided value is within the specified tolerance of a particular
      * instant. The duration is rendered as ISO-8601 by default, for example `"PT168H"`:
      *
      * {{{
      *   tomorrow is within( Duration.standardDays( 7L ) ).of( now )
      * }}}
      */
    def within[ T <: ReadableInstant ]( duration: ReadableDuration ): WithinBuilder[ T ] =
      within( duration, duration.toString )
  }
}
