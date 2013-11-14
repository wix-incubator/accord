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


package com.tomergabel.accord.dsl

import com.tomergabel.accord._
import com.tomergabel.accord.Failure
import com.tomergabel.accord.Violation

object Combinators {
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
