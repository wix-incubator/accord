/*
  Copyright 2013-2017 Wix.com

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

/** Adds support for [[org.joda.time.ReadableInstant ReadableInstants]] (and subclasses) to the Accord DSL.
  *
  * ==Usage==
  *
  * To use these extensions, import this package as follows:
  *
  * {{{
  *   import org.joda.time.DateTime
  *   import org.joda.time.Duration
  *
  *   case class Person( name: String, birthDate: DateTime )
  *
  *   // Import the Accord DSL and JODA extensions...
  *   import com.wix.accord.dsl._
  *   import com.wix.accord.joda._
  *
  *   // DateTime (and other instant types) are now supported
  *   implicit val personValidator = validator[ Person ] { p =>
  *     p.name is notEmpty
  *     p.birthDate is before( DateTime.now )
  *   }
  * }}}
  *
  * ==Combinators==

  * Supported operations:
  *
  * {{{
  *   // Simple equality/inequality
  *   val lastYear = DateTime.now.minus( Duration.standardDays( 365L ) )
  *   p.birthDate is equalTo( lastYear )
  *   p.birthDate is notEqualTo( lastYear )
  *
  *   // Equality with tolerance
  *   p.birthDate is within( Duration.standardDays( 7L ) ).of( lastYear )
  *
  *   // Before/after
  *   val ageOfAdulthood = DateTime.now.minus( Duration.standardDays( 18 * 365L ) )
  *   p.birthDate is before( ageOfAdulthood )
  *   p.birthDate is after( ageOfAdulthood )
  * }}}
  *
  */
package object joda extends ReadableInstantCombinators with ReadableInstantOps
