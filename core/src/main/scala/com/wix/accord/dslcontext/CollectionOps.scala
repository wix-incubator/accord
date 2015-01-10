/*
  Copyright 2013-2014 Wix.com

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

package com.wix.accord.dslcontext

import com.wix.accord.Domain
import com.wix.accord.combinators.CollectionCombinators.HasEmpty
import com.wix.accord.dslcontext.CollectionOps.HasSize

import scala.collection.GenTraversableOnce
import scala.language.implicitConversions

/** Provides a DSL for collection-like objects. Works in conjunction with [[com.wix.accord.dslcontext.DSL]]. */
trait CollectionOps {
  self: OrderingContext =>

  protected val domain: Domain
  import domain._

  /** Specifies a validator that succeeds on empty instances; the object under validation must implement
    * `def isEmpty: Boolean` (see [[com.wix.accord.combinators.HasEmpty]]).
    */
  def empty[ T <: AnyRef <% HasEmpty ]: Validator[ T ] = new Empty[ T ]

  /** Specifies a validator that fails on empty instances; the object under validation must implement
    * `def isEmpty: Boolean` (see [[com.wix.accord.combinators.HasEmpty]]).
    */
  def notEmpty[ T <: AnyRef <% HasEmpty ]: Validator[ T ] = new NotEmpty[ T ]

  /**
   * An implicit conversion to enable any collection-like object (e.g. strings, options) to be handled by
   * [[com.wix.accord.dslcontext.SizeContext]].
   *
   * [[java.lang.String]] does not directly implement `size` (in practice it is implemented in
   * [[scala.collection.IndexedSeqOptimized]] via an implicit conversion and an inheritance stack), and this is
   * a case where the Scala compiler does not always infer structural types correctly. By requiring
   * a view bound from `T` to [[scala.collection.GenTraversableOnce]] we can force any collection-like structure
   * to conform to the structural type [[com.wix.accord.dslcontext.CollectionOps.HasSize]], and by requiring
   * a view bound from `T` to [[com.wix.accord.dslcontext.CollectionOps.HasSize]] at the call site (via
   * [[com.wix.accord.dslcontext.SizeContext]]) we additionally support any class that directly
   * conforms to the structural type as well.
   *
   * @param gto An object that is, or is implicitly convertible to, [[scala.collection.GenTraversableOnce]].
   * @tparam T The type that conforms, directly or implicitly, to [[com.wix.accord.dslcontext.CollectionOps.HasSize]].
   * @return The specified object, strictly-typed as [[com.wix.accord.dslcontext.CollectionOps.HasSize]].
   */
  implicit def genericTraversableOnce2HasSize[ T ]( gto: T )( implicit ev: T => GenTraversableOnce[_] ): HasSize = gto

  /** Provides access to size-based validators (where the object under validation must implement
    * `def size: Int`, see [[com.wix.accord.dslcontext.CollectionOps.HasSize]]). Enables syntax such as
    * `c.students has size > 0`.
    */
  val size = new OrderingOps { override protected def snippet = "has size" }
}

object CollectionOps {
  /** A structural type representing any object that has a size. */
  type HasSize = Any { def size: Int }
}