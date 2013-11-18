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

import com.tomergabel.accord.{Violation, Validator}
import scala.language.implicitConversions

/** Combinators that operate on collections and collection-like structures. */
trait CollectionCombinators {

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
}
