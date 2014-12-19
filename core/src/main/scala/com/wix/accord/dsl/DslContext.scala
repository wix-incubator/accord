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

package com.wix.accord.dsl

import com.wix.accord.{Domain, Validation, Results}
import com.wix.accord.dsl.CollectionOps.HasSize

// TODO ScalaDocs

abstract sealed class DslContext[ Inner, Outer ]( implicit val domain: Validation with Results ) {
  import domain._

  protected def transform: Validator[ Inner ] => Validator[ Outer ]

  private def aggregate[ Coll, Element ]( validator: Validator[ Element ], aggregator: Traversable[ Result ] => Result )
                                        ( implicit ev: Coll => Traversable[ Element ] ): Validator[ Coll ] =
    new Validator[ Coll ] {
      def apply( col: Coll ) = if ( col == null ) nullFailure else aggregator( col map validator )
    }

  protected def all[ Coll, Element ]( validator: Validator[ Element ] )
                                    ( implicit ev: Coll => Traversable[ Element ] ): Validator[ Coll ] =
    aggregate( validator, r => ( r fold Success )( _ and _ ) )

  def is    ( validator: Validator[ Inner ] ): Validator[ Outer ] = transform apply validator
  def should( validator: Validator[ Inner ] ): Validator[ Outer ] = transform apply validator
  def must  ( validator: Validator[ Inner ] ): Validator[ Outer ] = transform apply validator

  /** Provides extended syntax for collections; enables validation rules such as `c.students.each is valid`.
    *
    * @param ev Evidence that the provided expression can be treated as a collection.
    * @tparam Element The element type m of the specified collection.
    * @return Additional syntax (see implementation).
    */
  def each[ Element ]( implicit ev: Inner => Traversable[ Element ] ) =
    new DslContext[ Element, Outer ] {
      private val innerToOuter = DslContext.this.transform.asInstanceOf[ domain.Validator[ Inner ] => domain.Validator[ Outer ] ]
      private val elementToInner = all[ Inner, Element ] _
      protected override def transform = elementToInner andThen innerToOuter
    }

  trait SizeContext {
    def apply( validator: Validator[ Int ] )( implicit ev: Inner => HasSize ): Validator[ Outer ] = {
      val composed = validator.boxed compose { u: Inner => if ( u == null ) null else u.size }
      transform apply composed
    }
  }

  object CollectionDslContext extends /*DslContext[ Inner, Outer ] with*/ SizeContext

  def has = CollectionDslContext
  def have = CollectionDslContext
}

trait SimpleDslContext[ U ] extends DslContext[ U, U ] {
  self: Validation with Results =>

  override def transform = identity
}

