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

import org.scalatest.{Matchers, WordSpec}

class CombinatorTests extends WordSpec with Matchers with ResultMatchers {
  import dsl.Combinators

  "Size combinator" should {
    case class Test( size: Int )

    "successfully validate a greater-than rule" in {
      val left = Test( 10 )
      val validator = new Combinators.Size[ Test ] > 5
      validator( left ) should be( aSuccess )
    }
    "render a correct greater-than rule violation" in {
      val left = Test( 0 )
      val validator = new Combinators.Size[ Test ] > 5
      validator( left ) should failWith( "has size 0, expected more than 5" )
    }
    "successfully validate a greater-than-or-equal rule" in {
      val left = Test( 10 )
      val validator = new Combinators.Size[ Test ] >= 10
      validator( left ) should be( aSuccess )
    }
    "render a correct greater-than-or-equal rule violation" in {
      val left = Test( 0 )
      val validator = new Combinators.Size[ Test ] >= 5
      validator( left ) should failWith( "has size 0, expected 5 or more" )
    }
    "successfully validate a lesser-than rule" in {
      val left = Test( 5 )
      val validator = new Combinators.Size[ Test ] < 10
      validator( left ) should be( aSuccess )
    }
    "render a correct lesser-than rule violation" in {
      val left = Test( 10 )
      val validator = new Combinators.Size[ Test ] < 10
      validator( left ) should failWith( "has size 10, expected less than 10" )
    }
    "successfully validate a lesser-than-or-equal rule" in {
      val left = Test( 10 )
      val validator = new Combinators.Size[ Test ] <= 10
      validator( left ) should be( aSuccess )
    }
    "render a correct lesser-than-or-equal rule violation" in {
      val left = Test( 10 )
      val validator = new Combinators.Size[ Test ] <= 5
      validator( left ) should failWith( "has size 10, expected 5 or less" )
    }
  }

  "Empty combinator" should {
    "successfully validate an empty object" in {
      val left = None
      val validator = new Combinators.Empty[ Option[ String ] ]
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = Some( "content" )
      val validator = new Combinators.Empty[ Option[ String ] ]
      validator( left ) should failWith( "must be empty" )
    }
  }

  "NotEmpty combinator" should {
    "successfully validate a non-empty object" in {
      val left = Some( "content" )
      val validator = new Combinators.NotEmpty[ Option[ String ] ]
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = None
      val validator = new Combinators.NotEmpty[ Option[ String ] ]
      validator( left ) should failWith( "must not be empty" )
    }
  }

  "StartsWith combinator" should {
    "successfully validate a string that starts with the specified prefix" in {
      val left = "ham and eggs"
      val validator = new Combinators.StartsWith( "ham" )
      validator( left ) should be( aSuccess )
    }
    "render a correct rule violation" in {
      val left = "eggs and ham"
      val validator = new Combinators.StartsWith( "ham" )
      validator( left ) should failWith( "must start with 'ham'" )
    }
  }

  "And combinator with a two-clause rule" should {
    val clause1 = new Combinators.Size[ String ] >= 4
    val clause2 = new Combinators.StartsWith( "ok" )
    val validator = new Combinators.And[ String ]( clause1, clause2 )
    
    "successfully validate a object that satisfies both clauses" in {
      validator( "okay" ) should be( aSuccess )
    }
    "render a correct rule violation when the first clause is not satisfied" in {
      validator( "ok" ) should failWith( "has size 2, expected 4 or more" )
    }
    "render a correct rule violation when the second clause is not satisfied" in {
      validator( "no such luck" ) should failWith( "must start with 'ok'" )
    }
    "render a correct rule violation when both clauses are not satisfied" in {
      validator( "no" ) should failWith( "has size 2, expected 4 or more", "must start with 'ok'" )
    }
  }

  "Or combinator with a two-clause rule" should {
    val clause1 = new Combinators.Size[ String ] >= 4
    val clause2 = new Combinators.StartsWith( "ok" )
    val validator = new Combinators.Or[ String ]( clause1, clause2 )

    "successfully validate a object that satisfies both clauses" in {
      validator( "okay" ) should be( aSuccess )
    }
    "successfully validate an object that only satisfies the first clause" in {
      validator( "dokey" ) should be( aSuccess )
    }
    "successfully validate an object that only satisfies the second clause" in {
      validator( "ok" ) should be( aSuccess )
    }
    "render a correct rule violation when both clauses are not satisfied" in {
      // TODO decide on the correct user story and rewrite the violation/reporting subsystem
      validator( "no" ) should failWith( "doesn't meet any of the requirements" )
    }
  }

  "Fail combinator" should {
    "render a correct rule violation" in {
      val validator = new Combinators.Fail[ String ]( "message" )
      validator( "whatever" ) should failWith( "message" )
    }
  }

  "Nil validator" should {
    "successfully validate an arbitrary object" in {
      val validator = new Combinators.NilValidator[ String ]
      validator( "whatever" ) should be( aSuccess )
    }
  }
}
