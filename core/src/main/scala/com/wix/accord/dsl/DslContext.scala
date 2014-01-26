/*
  Copyright 2013 Wix.com

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

trait DslContext[ U ]

trait BaseDslVerbs[ U ] {
  def is( validator: Validator[ _ >: U ] ) = validator
  def has( validator: Validator[ _ >: U ] ) = validator
  def have( validator: Validator[ _ >: U ] ) = validator
  def should( validator: Validator[ _ >: U ] ) = validator
  def must( validator: Validator[ _ >: U ] ) = validator
}

private object Aggregates {
  private def aggregate[ U, E ]( validator: Validator[ E ], aggregator: Traversable[ Result ] => Result )
                               ( implicit ev: U <:< Traversable[ E ] ) =
    new Validator[ U ] { def apply( col: U ) = aggregator( col map validator ) }

  def all[ U, E ]( validator: Validator[ E ] )( implicit ev: U <:< Traversable[ E ] ): Validator[ U ] =
    aggregate( validator, r => ( r fold Success )( _ and _ ) )
}


trait CollectionContext[ U ] {
  self: DslContext[ U ] =>

  /** Provides extended syntax for collections; enables validation rules such as `c.students.each is valid`.
    *
    * @param ev Evidence that the provided expression can be treated as a collection.
    * @tparam E The element type of the specified collection.
    * @return Additional syntax (see implementation).
    */
  def each[ E ]( implicit ev: U <:< Traversable[ E ] ) = new {
    def is( validator: Validator[ _ >: E ] ): Validator[ U ] = Aggregates.all( validator )
  }
}

