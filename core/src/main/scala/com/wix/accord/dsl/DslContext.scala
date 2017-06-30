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

package com.wix.accord.dsl

import com.wix.accord.{Result, Validator}
import com.wix.accord.ResultBuilders._

// TODO ScalaDocs

trait ContextTransformer[ Inner, Outer ] {
  protected def transform: Validator[ Inner ] => Validator[ Outer ]
}

class CollectionDslContext[ Inner, Outer ]( protected val transform: Validator[ Inner ] => Validator[ Outer ] )
  extends ContextTransformer[ Inner, Outer ] {

  def apply( validator: Validator[ Int ] )( implicit ev: Inner => HasSize ): Validator[ Outer ] = {
    val composed = new Validator[ Inner ] {
      override def apply( v1: Inner ): Result =
        if ( v1 == null )
          Validator.nullFailure
        else
          validator.boxed( v1.size ).withValue( v1 )
    }
    transform apply composed
  }
}

trait IndexedDescriptions[ T ] {
  def includeIndexInformation: Boolean
}

trait FallbackIndexDescriptions {
  implicit def disableIndexDescriptionsByDefault[ T ] = new IndexedDescriptions[ T ] {
    def includeIndexInformation: Boolean = false
  }
}

object IndexedDescriptions extends FallbackIndexDescriptions {
  implicit def enableIndexingForSequences[ T ]( implicit ev: T => Seq[_] ): IndexedDescriptions[ T ] =
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
  protected override def transform = identity
}

