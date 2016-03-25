/*
  Copyright 2013-2015 Wix.com

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

import com.wix.accord.{Success, Result, Validator}

// TODO ScalaDocs

trait ContextTransformer[ Inner, Outer ] {
  protected def transform: Validator[ Inner ] => Validator[ Outer ]
}

private object Aggregates {
  private def aggregate[ Coll, Element ]( validator: Validator[ Element ], aggregator: Traversable[ Result ] => Result )
                                        ( implicit ev: Coll => Traversable[ Element ] ): Validator[ Coll ] =
    new Validator[ Coll ] {
      def apply( col: Coll ) =
        if ( col == null )
          Validator.nullFailure
        else
          aggregator(
            col.foldLeft( Traversable.newBuilder[ Result ] -> 0 ) {
              case ( ( acc, i ), e ) =>
                ( acc += validator( e ).withDescription( _ map { _ + s" [at index $i]" } ) ) -> ( i + 1 )
            }._1.result()
          )
    }

  def all[ Coll, Element ]( validator: Validator[ Element ] )
                          ( implicit ev: Coll => Traversable[ Element ] ): Validator[ Coll ] =
    aggregate( validator, r => ( r fold Success )( _ and _ ) )
}

trait SizeContext[ Inner, Outer ] {
  self: ContextTransformer[ Inner, Outer ] =>

  def apply( validator: Validator[ Int ] )( implicit ev: Inner => HasSize ): Validator[ Outer ] = {
    val composed = validator.boxed compose { u: Inner => if ( u == null ) null else u.size }
    transform apply composed
  }
}

class CollectionDslContext[ Inner, Outer ]( protected val transform: Validator[ Inner ] => Validator[ Outer ] )
  extends SizeContext[ Inner, Outer ] with ContextTransformer[ Inner, Outer ]

trait DslContext[ Inner, Outer ] {
  self: ContextTransformer[ Inner, Outer ] =>

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
    new DslContext[ Element, Outer ] with ContextTransformer[ Element, Outer ] {
      protected override def transform = self.transform compose Aggregates.all[ Inner, Element ]
    }

  private val collContext = new CollectionDslContext( transform )
  def has: CollectionDslContext[ Inner, Outer ] = collContext
  def have: CollectionDslContext[ Inner, Outer ] = collContext
}

trait SimpleDslContext[ U ] extends DslContext[ U, U ] with ContextTransformer[ U, U ] {
  override def transform = identity
}

