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

import scala.language.experimental.macros
import com.tomergabel.accord.transform.ValidationTransform

package object dsl {
  import Combinators._

  def validator[ T ]( v: T => Unit ): Validator[ T ] = macro ValidationTransform.apply[ T ]// validator_impl[ T ]

  implicit class Contextualizer[ U ]( value: U ) {
    def is( validator: Validator[ U ] ) = validator
    def has( validator: Validator[ U ] ) = validator
    def have( validator: Validator[ U ] ) = validator

    class TraversableExtensions[ E ]( implicit ev: U <:< Traversable[ E ] ) {
      private def aggregate( validator: Validator[ E ], aggregator: Traversable[ Result ] => Result ) = new Validator[ U ] {
        def apply( col: U ) = aggregator( ev( col ) map validator )
      }

      def all( validator: Validator[ E ] ): Validator[ U ] = aggregate( validator, r => ( r fold Success )( _ and _ ) )
    }

    def are[ E ]( implicit ev: U <:< Traversable[ E ] ) = new TraversableExtensions[ E ]
    def each[ E ]( implicit ev: U <:< Traversable[ E ] ) = new TraversableExtensions[ E ] {
      def is( validator: Validator[ E ] ): Validator[ U ] = all( validator )
    }
  }

  implicit class ExtendValidator[ T ]( validator: Validator[ T ] ) {
    def and( other: Validator[ T ] ) = new And( validator, other ) // TODO shortcut multiple ANDs
    def or( other: Validator[ T ] ) = new Or( validator, other )   // TODO shortcut multiple ORs
  }

  def empty[ T <: HasEmpty ]: Validator[ T ] = new Empty[ T ]
  def notEmpty[ T <: HasEmpty ]: Validator[ T ] = new NotEmpty[ T ]
  def size[ T <: HasSize ] = new Size[ T ]
  def valid[ T ]( implicit validator: Validator[ T ] ): Validator[ T ] = validator
}
