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

package com.wix.accord

import org.scalatest.{FlatSpec, Matchers}

class DescriptionCombinationSpec extends FlatSpec with Matchers {
  import Descriptions._

  private val indexed = Indexed( 1, Empty )
  private val explicit = Explicit( "test" )
  private val generic = Generic( "test" )
  private val accessChain = AccessChain( Generic( "a" ), Generic( "b" ), Generic( "c" ) )

  "Any combination of Empty" should "return the other description as-is" in {
    combine( Empty, Empty ) shouldEqual Empty
    combine( Empty, indexed ) shouldEqual indexed
    combine( Empty, explicit ) shouldEqual explicit
    combine( Empty, generic ) shouldEqual generic
    combine( Empty, accessChain ) shouldEqual accessChain
    combine( Empty, SelfReference ) shouldEqual SelfReference
    combine( Empty, Empty ) shouldEqual Empty
    combine( indexed, Empty ) shouldEqual indexed
    combine( explicit, Empty ) shouldEqual explicit
    combine( generic, Empty ) shouldEqual generic
    combine( accessChain, Empty ) shouldEqual accessChain
    combine( SelfReference, Empty ) shouldEqual SelfReference
  }

  "Any combination of Indexed (of Empty)" should "replace the Empty inner description with the specified one" in {
    combine( indexed, indexed ) shouldEqual Indexed( 1, indexed )
    combine( indexed, explicit ) shouldEqual Indexed( 1, explicit )
    combine( indexed, generic ) shouldEqual Indexed( 1, generic )
    combine( indexed, accessChain ) shouldEqual Indexed( 1, accessChain )
    combine( indexed, SelfReference ) shouldEqual Indexed( 1, SelfReference )
    combine( indexed, indexed ) shouldEqual Indexed( 1, indexed )
    combine( explicit, indexed ) shouldEqual Indexed( 1, explicit )
    combine( generic, indexed ) shouldEqual Indexed( 1, generic )
    combine( accessChain, indexed ) shouldEqual Indexed( 1, accessChain )
    combine( SelfReference, indexed ) shouldEqual Indexed( 1, SelfReference )
  }

  "Any combination of SelfReference" should "return the other description as-is" in {
    combine( SelfReference, explicit ) shouldEqual explicit
    combine( SelfReference, generic ) shouldEqual generic
    combine( SelfReference, accessChain ) shouldEqual accessChain
    combine( SelfReference, SelfReference ) shouldEqual SelfReference
    combine( explicit, SelfReference ) shouldEqual explicit
    combine( generic, SelfReference ) shouldEqual generic
    combine( accessChain, SelfReference ) shouldEqual accessChain
  }

  "Combining a non-empty description with an AccessChain" should "produce a new AccessChain indirecting over the description" in {
    val ind = Generic( "indirection" )
    val nonEmptyIndex = Indexed( 1, Generic( "coll" ) )

    combine( explicit, AccessChain( ind ) ) shouldEqual AccessChain( explicit, ind )
    combine( generic, AccessChain( ind ) ) shouldEqual AccessChain( generic, ind )
    combine( nonEmptyIndex, AccessChain( ind ) ) shouldEqual AccessChain( nonEmptyIndex, ind )
  }

  "Combining an AccessChain with another AccessChain" should "produce a new AccessChain indirecting right-to-left" in {
    // AccessChains can be generated in one of two ways: directly (when ExpressionDescriber runs into a.b.c), or
    // indirectly via Result.applyDescription. In the latter case, the innermost description is actually on the right
    // side of the indirection (so a.b.c => c applyDescription b applyDescription a), hence the reverse indirection
    // order.
    // See issue #66 (https://github.com/wix/accord/issues/66) for an example use case.

    val a = Generic( "a" )
    val b = Generic( "b" )
    combine( AccessChain( a ), AccessChain( b ) ) shouldEqual AccessChain( b, a )
  }

  "Explicit description" should "combine with nothing" in {
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( explicit, generic ) }
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( explicit, explicit ) }
  }
}