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

trait TemporalOps {
  def before[ T <: Temporal ]( right: T ) = new Before( right )
  def after[ T <: Temporal ]( right: T ) = new After( right )

  class WithinBuilder[ T <: Temporal ] private[ TemporalOps ]( duration: Duration, friendlyDuration: => String ) {
    def of( target: T ): Within[ T ] = new Within( target, duration, friendlyDuration )
  }

  def within[ T <: Temporal ]( count: Long, timeUnit: TemporalUnit ): WithinBuilder[ T ] =
    new WithinBuilder[ T ]( timeUnit.getDuration.multipliedBy( count ), s"$count ${ timeUnit.toString.toLowerCase }" )

  def within[ T <: Temporal ]( duration: Duration, friendlyDuration: => String ): WithinBuilder[ T ] =
    new WithinBuilder[ T ]( duration, friendlyDuration )

  def within[ T <: Temporal ]( duration: Duration ): WithinBuilder[ T ] =
    within( duration, duration.toString )
}
