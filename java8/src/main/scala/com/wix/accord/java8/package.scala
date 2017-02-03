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

/** Adds support for [[java.time.temporal.Temporal temporals]] (and subclasses) to the Accord DSL.
  *
  * ==Usage==
  *
  * To use these extensions, import this package as follows:
  *
  * {{{
  *   import java.time.LocalDateTime
  *   import java.time.temporal.ChronoUnit
  *   import java.time.Duration
  *
  *   case class Person( name: String, birthDate: LocalDateTime )
  *
  *   // Import the Accord DSL and Java 8 extensions...
  *   import com.wix.accord.dsl._
  *   import com.wix.accord.java8._
  *
  *   // LocalDateTime (and other temporals) are now supported
  *   implicit val personValidator = validator[ Person ] { p =>
  *     p.name is notEmpty
  *     p.birthDate is before( LocalDateTime.now )
  *   }
  * }}}
  *
  * ==Combinators==

  * Supported operations:
  *
  * {{{
  *   // Simple equality/inequality
  *   val lastYear = LocalDateTime.now.minus( 1L, ChronoUnit.YEARS )
  *   p.birthDate is equalTo( lastYear )
  *   p.birthDate is notEqualTo( lastYear )
  *
  *   // Equality with tolerance (both variants are equivalent)
  *   val oneWeek = Duration.ofDays( 7L )
  *   p.birthDate is within( oneWeek ).of( lastYear )
  *   p.birthDate is within( 7L, ChronoUnit.DAYS ).of( lastYear )
  *
  *   // Before/after
  *   val ageOfAdulthood = LocalDateTime.now.minus( 18L, ChronoUnit.YEARS )
  *   p.birthDate is before( ageOfAdulthood )
  *   p.birthDate is after( ageOfAdulthood )
  *
  *   // Arithmetic-style operations
  *   p.birthDate must be <= ageOfAdulthood
  *   p.birthDate is between( lastYear, LocalDateTime.now.minus( oneWeek ) )
  * }}}
  *
  */
package object java8 extends TemporalCombinators with TemporalOps
