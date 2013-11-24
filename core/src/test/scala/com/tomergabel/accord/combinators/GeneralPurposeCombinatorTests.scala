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

package com.tomergabel.accord.combinators


class GeneralPurposeCombinatorTests extends CombinatorTestSpec {

  "And combinator with a two-clause rule" should {
    val clause1 = new Size[ String ] >= 4
    val clause2 = new StartsWith( "ok" )
    val validator = new And[ String ]( clause1, clause2 )

    "successfully validate a object that satisfies both clauses" in {
      validator( "okay" ) should be( aSuccess )
    }
    "render a correct rule violation when the first clause is not satisfied" in {
      validator( "ok" ) should failWith( testContext -> "has size 2, expected 4 or more" )
    }
    "render a correct rule violation when the second clause is not satisfied" in {
      validator( "no such luck" ) should failWith( testContext -> "must start with 'ok'" )
    }
    "render a correct rule violation when both clauses are not satisfied" in {
      validator( "no" ) should failWith(
        testContext -> "has size 2, expected 4 or more",
        testContext -> "must start with 'ok'" )
    }
  }

  "Or combinator with a two-clause rule" should {
    val clause1 = new Size[ String ] >= 4
    val clause2 = new StartsWith( "ok" )
    val validator = new Or[ String ]( clause1, clause2 )

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
      validator( "no" ) should failWith( group( testContext, "doesn't meet any of the requirements",
        testContext -> "has size 2, expected 4 or more",
        testContext -> "must start with 'ok'"
      ) )
    }
  }

  "Fail combinator" should {
    "render a correct rule violation" in {
      val validator = new Fail[ String ]( "message" )
      validator( "whatever" ) should failWith( testContext -> "message" )
    }
  }

  "Nil validator" should {
    "successfully validate an arbitrary object" in {
      val validator = new NilValidator[ String ]
      validator( "whatever" ) should be( aSuccess )
    }
  }
}
