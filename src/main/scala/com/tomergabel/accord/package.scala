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

package com.tomergabel

import scala.language.experimental.macros

/**
 * Created by tomer on 8/7/13.
 */
package object accord {
  // TODO work on the description/messaging infrastructure

  case class Violation( constraint: String, value: Any )

  sealed trait Result {
    def and( other: Result ): Result
    def or( other: Result ): Result
  }
  case object Success extends Result {
    def and( other: Result ) = other
    def or( other: Result ) = this
  }
  case class Failure( violations: Seq[ Violation ] ) extends Result {
    def and( other: Result ) = other match {
      case Success => this
      case Failure( vother ) => Failure( violations ++ vother )
    }
    def or( other: Result ) = other match {
      case Success => other
      case Failure( vother ) => Failure( vother )
    }
  }

  type Validator[ T ] = T => Result
  def validate[ T ]( x: T )( implicit validator: Validator[ T ] ) = validator( x )

  object combinators {
    private[ accord ] def result( test: => Boolean, violation: => Violation ) =
      if ( test ) Success else Failure( Seq( violation ) )

    type HasEmpty = { def isEmpty(): Boolean }
    class Empty[ T <: HasEmpty ] extends Validator[ T ] {
      def apply( x: T ) = result( x.isEmpty(), Violation( "must be empty", x ) )
    }

    type HasSize = { def size: Int }
    class Size[ T <: HasSize ] {
      def >( other: Int ) = new Validator[ T ] {
        def apply( x: T ) = result( x.size > other, Violation( s"has size ${x.size}, expected more than $other", x ) )
      }
    }

    class NotEmpty[ T <: HasEmpty ] extends Validator[ T ] {
      def apply( x: T ) = result( !x.isEmpty, Violation( "must not be empty", x ) )
    }

    class And[ T ]( predicates: Validator[ T ]* ) extends Validator[ T ] {
      def apply( x: T ) = predicates.map { _ apply x }.fold( Success ) { _ and _ }
    }

    class Or[ T ]( predicates: Validator[ T ]* ) extends Validator[ T ] {
      def apply( x: T ) = predicates.map { _ apply x }.fold( Success ) { _ or _ }
      // TODO rethink resulting violation
    }

    class Fail[ T ]( message: => String ) extends Validator[ T ] {
      def apply( x: T ) = result( test = false, Violation( message, x ) )
    }

    class NilValidator[ T ] extends Validator[ T ] {
      def apply( x: T ) = Success
    }
  }


  object builder {
    import combinators._

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

    def empty[ T <: HasEmpty ] = new Empty[ T ]
    def notEmpty[ T <: HasEmpty ] = new NotEmpty[ T ]
    def size[ T <: HasSize ] = new Size[ T ]
    def valid[ T ]( implicit validator: Validator[ T ] ) = validator
  }
}
