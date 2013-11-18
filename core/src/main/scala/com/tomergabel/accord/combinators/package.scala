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


package com.tomergabel.accord

/** Provides implementations of various validator combinators for use by the DSL. Can, though not intended to, be
  * used directly by end-user code.
  */
package object combinators {

  /** A helper method to simplify rendering results.
    *
    * @param test The validation test. If it succeeds, [[com.tomergabel.accord.Success]] is returned, otherwise
    *             a [[com.tomergabel.accord.Failure]] is generated based on the specified violation generator.
    * @param violation A generator for a validation violation. Only called if the test fails.
    * @return A [[com.tomergabel.accord.Result]] instance with the results of the validation.
    */
  private[ accord ] def result( test: => Boolean, violation: => Violation ) =
    if ( test ) Success else Failure( Seq( violation ) )

  /**
   * A useful helper class when validating numerical properties (size, length, arity...). Provides the usual
   * arithmetic operators in a consistent manner across all numerical property combiners. Violation messages
   * are structured based on the provided snippet and the property/constraint values, for example:
   *
   * ```
   * class StringLength extends NumericPropertyWrapper[ String, Int, String ]( _.length, "has length" )
   *
   * val result = ( new StringLegnth < 16 ) apply "This text is too long"
   * // result is a Failure; violation text: "has length 21, expected 16 or less".
   * ```
   *
   * Additionally provides a representation type `Repr` to shortcut type inference; for an expression like
   * `c.students has size > 0`, the object under validation is `c.students` but its inferred type isn't resolved
   * until the leaf node of the expression tree (in this case, the method call `> 0`). That means all constraints,
   * view bounds and the like have to exist at the leaf node, or in other words on the method call itself; to
   * generalize this, each arithmetic method requires an implicit `T => Repr` conversion, and because `Repr`
   * is specified by whomever instantiates [[com.tomergabel.accord.combinators.NumericPropertyWrapper]] the
   * view bound is placed correctly at the call site. (*whew*, hope that made sense)
   *
   * @param extractor A function which extracts the property value from the representation of the object under
   *                  validation (e.g. `( p: String ) => p.length`)
   * @param snippet A textual snippet describing what the validator does (e.g. `has length`)
   * @param ev Evidence that the property is of a numeric type (normally filled in by the compiler automatically)
   * @tparam T The type of the object under validation
   * @tparam P The type of the property under validation
   * @tparam Repr The runtime representation of the object under validation. When it differs from `T`, an implicit
   *              conversion `T => Repr` is required ''at the call site''.
   */
  class NumericPropertyWrapper[ T, P, Repr ]( extractor: Repr => P, snippet: String )( implicit ev: Numeric[ P ] ) {
    /** Generates a validator that succeeds only if the property value is greater than the specified bound. */
    def >( other: P )( implicit repr: T => Repr ) = new Validator[ T ] {
      def apply( x: T ) = {
        val v = ( repr andThen extractor )( x )
        result( ev.gt( v, other ), Violation( s"$snippet $v, expected more than $other", x ) )
      }
    }
    /** Generates a validator that succeeds only if the property value is less than the specified bound. */
    def <( other: P )( implicit repr: T => Repr ) = new Validator[ T ] {
      def apply( x: T ) = {
        val v = ( repr andThen extractor )( x )
        result( ev.lt( v, other ), Violation( s"$snippet $v, expected less than $other", x ) )
      }
    }
    /** Generates a validator that succeeds if the property value is greater than or equal to the specified bound. */
    def >=( other: P )( implicit repr: T => Repr ) = new Validator[ T ] {
      def apply( x: T ) = {
        val v = ( repr andThen extractor )( x )
        result( ev.gteq( v, other ), Violation( s"$snippet $v, expected $other or more", x ) )
      }
    }
    /** Generates a validator that succeeds if the property value is less than or equal to the specified bound. */
    def <=( other: P )( implicit repr: T => Repr ) = new Validator[ T ] {
      def apply( x: T ) = {
        val v = ( repr andThen extractor )( x )
        result( ev.lteq( v, other ), Violation( s"$snippet $v, expected $other or less", x ) )
      }
    }
  }

  import scala.language.implicitConversions

  /** A structural type representing any object that can be empty. */
  type HasEmpty = { def isEmpty: Boolean }

  /**
   * An implicit conversion to enable any collection-like object (e.g. strings, options) to be handled by the
   * [[com.tomergabel.accord.combinators.Empty]] and [[com.tomergabel.accord.combinators..NotEmpty]]
   * combinators.
   *
   * [[java.lang.String]] does not directly implement `isEmpty` (in practice it is implemented in
   * [[scala.collection.IndexedSeqOptimized]], via an implicit conversion and an inheritance stack), and this is
   * a case where the Scala compiler does not always infer structural types correctly. By requiring
   * a view bound from `T` to [[scala.collection.GenTraversableOnce]] we can force any collection-like structure
   * to conform to the structural type [[com.tomergabel.accord.combinators.HasEmpty]], and by requiring
   * a view bound from `T` to [[com.tomergabel.accord.combinators.HasEmpty]] at the call site (e.g.
   * [[com.tomergabel.accord.dsl.empty]]) we additionally support any class that directly conforms to the
   * structural type as well.
   *
   * @param gto An object that is, or is implicitly convertible to, [[scala.collection.GenTraversableOnce]].
   * @tparam T The type that conforms, directly or implicitly, to [[com.tomergabel.accord.combinators.HasEmpty]].
   * @return The specified object, strictly-typed as [[com.tomergabel.accord.combinators.HasEmpty]].
   */
  implicit def genericTraversableOnce2HasEmpty[ T <% scala.collection.GenTraversableOnce[_] ]( gto: T ): HasEmpty = gto

  /** A validator that operates on objects that can be empty, and succeeds only if the provided instance is
    * empty.
    * @tparam T A type that implements `isEmpty: Boolean` (see [[com.tomergabel.accord.combinators.HasEmpty]]).
    * @see [[com.tomergabel.accord.combinators.NotEmpty]]
    */
  class Empty[ T <% HasEmpty ] extends Validator[ T ] {
    def apply( x: T ) = result( x.isEmpty, Violation( "must be empty", x ) )
  }

  /** A validator that operates on objects that can be empty, and succeeds only if the provided instance is ''not''
    * empty.
    * @tparam T A type that implements `isEmpty: Boolean` (see [[com.tomergabel.accord.combinators.HasEmpty]]).
    * @see [[com.tomergabel.accord.combinators.Empty]]
    */
  class NotEmpty[ T <% HasEmpty ] extends Validator[ T ] {
    def apply( x: T ) = result( !x.isEmpty, Violation( "must not be empty", x ) )
  }

  /** A structural type representing any object that has a size. */
  type HasSize = { def size: Int }

  /**
   * An implicit conversion to enable any collection-like object (e.g. strings, options) to be handled by the
   * [[com.tomergabel.accord.combinators.Size]] combinator.
   *
   * [[java.lang.String]] does not directly implement `size` (in practice it is implemented in
   * [[scala.collection.IndexedSeqOptimized]], via an implicit conversion and an inheritance stack), and this is
   * a case where the Scala compiler does not always infer structural types correctly. By requiring
   * a view bound from `T` to [[scala.collection.GenTraversableOnce]] we can force any collection-like structure
   * to conform to the structural type [[com.tomergabel.accord.combinators.HasSize]], and by requiring
   * a view bound from `T` to [[com.tomergabel.accord.combinators.HasSize]] at the call site (i.e.
   * [[com.tomergabel.accord.dsl.size]]) we additionally support any class that directly conforms to the
   * structural type as well.
   *
   * @param gto An object that is, or is implicitly convertible to, [[scala.collection.GenTraversableOnce]].
   * @tparam T The type that conforms, directly or implicitly, to [[com.tomergabel.accord.combinators.HasSize]].
   * @return The specified object, strictly-typed as [[com.tomergabel.accord.combinators.HasSize]].
   */
  implicit def genericTraversableOnce2HasSize[ T <% scala.collection.GenTraversableOnce[_] ]( gto: T ): HasSize = gto

  /** A wrapper that operates on objects that provide a size, and provides validators based on te size of the
    * provided instance.
    * @tparam T A type that implements `size: Int` (see [[com.tomergabel.accord.combinators.HasSize]]).
    */
  class Size[ T ] extends NumericPropertyWrapper[ T, Int, HasSize ]( _.size, "has size" )

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
    // TODO this is a very poor solution. The violation/reporting subsystem needs significant rework.
    def apply( x: T ) = {
      val base = Failure( Violation( "doesn't meet any of the requirements", x ) :: Nil )
      predicates.map { _ apply x }.fold( base ) { _ or _ }
    }
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

  /** A validator that succeeds only if the provided string starts with the specified prefix. */
  class StartsWith( prefix: String ) extends Validator[ String ] {
    def apply( x: String ) = result( x startsWith prefix, Violation( s"must start with '$prefix'", x ) )
  }
}
