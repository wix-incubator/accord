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

import com.wix.accord.{Result, Success, Validator}

// TODO ScalaDocs

trait ContextTransformer[ Inner, Outer ] {
  protected def transform: Validator[ Inner ] => Validator[ Outer ]
}

private object Aggregates {

  def all[ Coll, Element ]( includeIndices: Boolean = true )( validator: Validator[ Element ] )
                          ( implicit ev: Coll => Traversable[ Element ] ): Validator[ Coll ] =
    new Validator[ Coll ] {
      def apply( coll: Coll ): Result =
        if ( coll == null ) Validator.nullFailure
        else {
          var index = 0
          val appendIndex: ( Option[ String ] => Option[ String ] ) =
            if ( includeIndices ) { prefix: Option[ String ] => Some( prefix.getOrElse( "" ) + s" [at index $index]" ) }
            else identity

          var aggregate: Result = Success
          coll foreach { element =>
            val result = validator apply element withDescription appendIndex
            aggregate = aggregate and result
            index = index + 1
          }

          aggregate
        }
    }
}

class CollectionDslContext[ Inner, Outer ]( protected val transform: Validator[ Inner ] => Validator[ Outer ] )
  extends ContextTransformer[ Inner, Outer ] {

  def apply( validator: Validator[ Int ] )( implicit ev: Inner => HasSize ): Validator[ Outer ] = {
    val composed = validator.boxed compose { u: Inner => if ( u == null ) null else u.size }
    transform apply composed
  }
}

trait IndexedDescriptions[ T ] {
  def includeIndexInformation: Boolean
}

object IndexedDescriptions {
  implicit def disableIndexingForOptions[ T ]: IndexedDescriptions[ Option[ T ] ] =
    new IndexedDescriptions[ Option[ T ] ] { def includeIndexInformation: Boolean = false }

  implicit def enableIndexingForCollections[ T ]( implicit ev: T => Traversable[_] ): IndexedDescriptions[ T ] =
    new IndexedDescriptions [ T ] { def includeIndexInformation: Boolean = true }
}

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
  def each[ Element ]( implicit ev: Inner => Traversable[ Element ], withIndices: IndexedDescriptions[ Inner ] ) =
    new DslContext[ Element, Outer ] with ContextTransformer[ Element, Outer ] {
      protected override def transform =
        self.transform compose Aggregates.all[ Inner, Element ]( withIndices.includeIndexInformation )
    }

  private val collContext = new CollectionDslContext( transform )
  def has: CollectionDslContext[ Inner, Outer ] = collContext
  def have: CollectionDslContext[ Inner, Outer ] = collContext
}

trait SimpleDslContext[ U ] extends DslContext[ U, U ] with ContextTransformer[ U, U ] {
  override def transform = identity
}

