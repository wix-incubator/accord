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

import com.wix.accord.dsl.OrderingOps

/** Provides a DSL for validating [[java.time.temporal.Temporal temporals]] (and subclasses thereof). */
trait TemporalOps {
  /** Generates a validator that succeeds only if the provided value comes strictly before the specified bound. */
  def before[ T <: Temporal ]( right: T ) = new Before( right )
  /** Generates a validator that succeeds only if the provided value comes strictly after the specified bound. */
  def after[ T <: Temporal ]( right: T ) = new After( right )

  /** A builder to support the `within` DSL extensions. */
  class WithinBuilder[ T <: Temporal ] private[ TemporalOps ]( duration: Duration, friendlyDuration: => String ) {
    /** Specifies the target temporal for the comparison. */
    def of( target: T ): Within[ T ] = new Within( target, duration, friendlyDuration )
  }

  /** Extends the Accord DSL with additional `within` operations over temporals. */
  implicit class ExtendAccordDSL( dsl: OrderingOps ) {
    /** Builds a validator that succeeds if the provided value is within the specified tolerance of a particular
      * temporal. For example:
      *
      * {{{
      *   tomorrow is within( 1L, ChronoUnit.WEEKS ).of( now )
      * }}}
      */
    def within[ T <: Temporal ]( count: Long, timeUnit: TemporalUnit ): WithinBuilder[ T ] =
      new WithinBuilder[ T ]( timeUnit.getDuration.multipliedBy( count ), s"$count ${ timeUnit.toString.toLowerCase }" )

    /** Builds a validator that succeeds if the provided value is within the specified tolerance of a particular
      * temporal. For example:
      *
      * {{{
      *   tomorrow is within( Duration.ofDays( 7L ), "7 days" ).of( now )
      * }}}
      */
    def within[ T <: Temporal ]( duration: Duration, friendlyDuration: => String ): WithinBuilder[ T ] =
      new WithinBuilder[ T ]( duration, friendlyDuration )

    /** Builds a validator that succeeds if the provided value is within the specified tolerance of a particular
      * temporal. The duration is rendered as ISO-8601 by default, for example `"PT168H"`. For example:
      *
      * {{{
      *   tomorrow is within( Duration.ofDays( 7L ) ).of( now )
      * }}}
      */
    def within[ T <: Temporal ]( duration: Duration ): WithinBuilder[ T ] =
      within( duration, duration.toString )
  }
}
