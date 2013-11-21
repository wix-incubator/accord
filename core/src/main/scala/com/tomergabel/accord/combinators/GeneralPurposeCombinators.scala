/*
  Copyright 2013 Tomer Gabel

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


package com.tomergabel.accord.combinators

import com.tomergabel.accord._

/** Non type-specific combinators. */
trait GeneralPurposeCombinators {
  /** A combinator that takes a chain of predicates and implements logical AND between them.
    * @param predicates The predicates to chain together.
    * @tparam T The type on which this validator operates.
    */
  class And[ T ]( predicates: Validator[ T ]* ) extends Validator[ T ] {
    def apply( x: T ) = predicates.map { _ apply x }.fold( Success ) { _ and _ }
  }

  /** A combinator that takes a chain of predicates and implements logical OR between them.
    * @param predicates The predicates to chain together.
    * @tparam T The type on which this validator operates.
    */
  class Or[ T ]( predicates: Validator[ T ]* ) extends Validator[ T ] {
    def apply( x: T ) = {
      val results = predicates.map { _ apply x }
      result( results contains Success,
        GroupViolation( x, "doesn't meet any of the requirements",
          results.collect { case Failure( violations ) => violations }.flatten ) )
    }
  }

  /** A validator that always fails with a specific violation.
    * @param message The violation message.
    * @tparam T The type on which this validator operates.
    */
  class Fail[ T ]( message: => String ) extends Validator[ T ] {
    def apply( x: T ) = result( test = false, violation( x, message ) )
  }

  /** A validator that always succeeds.
    * @tparam T The type on which this validator operates.
    */
  class NilValidator[ T ] extends Validator[ T ] {
    def apply( x: T ) = Success
  }

}