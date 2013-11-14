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


package com.tomergabel.accord.dsl

import com.tomergabel.accord._
import com.tomergabel.accord.Failure
import com.tomergabel.accord.Violation

/** Provides implementations of various validator combinators for use by the DSL. Can, though not intended to, be
  * used directly by end-user code.
  */
object Combinators {

  /** A helper method to simplify rendering results.
    *
    * @param test The validation test. If it succeeds, [[com.tomergabel.accord.Success]] is returned, otherwise
    *             a [[com.tomergabel.accord.Failure]] is generated based on the specified violation generator.
    * @param violation A generator for a validation violation. Only called if the test fails.
    * @return A [[com.tomergabel.accord.Result]] instance with the results of the validation.
    */
  private[ accord ] def result( test: => Boolean, violation: => Violation ) =
    if ( test ) Success else Failure( Seq( violation ) )

  type HasEmpty = { def isEmpty(): Boolean }

  /** A validator that operates on objects that can be empty, and succeeds only if the provided instance is
    * empty.
    * @tparam T A type that implements `isEmpty: Boolean` (see [[com.tomergabel.accord.dsl.Combinators.HasEmpty]]).
    * @see [[com.tomergabel.accord.dsl.Combinators.NotEmpty]]
    */
  class Empty[ T <: HasEmpty ] extends Validator[ T ] {
    def apply( x: T ) = result( x.isEmpty(), Violation( "must be empty", x ) )
  }

  type HasSize = { def size: Int }

  /** A wrapper that operates on objects that provide a size, and provides validators based on te size of the
    * provided instance.
    * @tparam T A type that implements `size: Int` (see [[com.tomergabel.accord.dsl.Combinators.HasSize]]).
    */
  class Size[ T <: HasSize ] {

    /** Generates a validator that succeeds only if the specified instance's size is larger than the specified value. */
    def >( other: Int ) = new Validator[ T ] {
      def apply( x: T ) = result( x.size > other, Violation( s"has size ${x.size}, expected more than $other", x ) )
    }
  }

  /** A validator that operates on objects that can be empty, and succeeds only if the provided instance is ''not''
    * empty.
    * @tparam T A type that implements `isEmpty: Boolean` (see [[com.tomergabel.accord.dsl.Combinators.HasEmpty]]).
    * @see [[com.tomergabel.accord.dsl.Combinators.Empty]]
    */
  class NotEmpty[ T <: HasEmpty ] extends Validator[ T ] {
    def apply( x: T ) = result( !x.isEmpty, Violation( "must not be empty", x ) )
  }

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
    def apply( x: T ) = predicates.map { _ apply x }.fold( Success ) { _ or _ }
    // TODO rethink resulting violation
  }

  /** A validator that always fails with a specific violation.
    * @param message The violation message.
    * @tparam T The type on which this validator operates.
    */
  class Fail[ T ]( message: => String ) extends Validator[ T ] {
    def apply( x: T ) = result( test = false, Violation( message, x ) )
  }

  /** A validator that always succeeds.
    * @tparam T The type on which this validator operates.
    */
  class NilValidator[ T ] extends Validator[ T ] {
    def apply( x: T ) = Success
  }
}
