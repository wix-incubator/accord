/*
  Copyright 2013-2016 Wix.com

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

  private val explicit = Explicit( "test" )
  private val generic = Generic( "test" )
  private val openIndexed = Indexed( 1, Empty )
  private val materializedIndexed = Indexed( 1, generic )
  private val accessChain = AccessChain( Generic( "a" ), Generic( "b" ), Generic( "c" ) )

  "Any combination of Empty" should "return the other description as-is" in {
    combine( Empty, Empty ) shouldEqual Empty
    combine( Empty, openIndexed ) shouldEqual openIndexed
    combine( Empty, explicit ) shouldEqual explicit
    combine( Empty, generic ) shouldEqual generic
    combine( Empty, accessChain ) shouldEqual accessChain
    combine( Empty, SelfReference ) shouldEqual SelfReference
  }

  "Combining an open Indexed (of==Empty) with any description" should
    "materialize a non-empty Indexed description" in {
    combine( openIndexed, openIndexed ) shouldEqual Indexed( 1, openIndexed )
    combine( openIndexed, explicit ) shouldEqual Indexed( 1, explicit )
    combine( openIndexed, generic ) shouldEqual Indexed( 1, generic )
    combine( openIndexed, accessChain ) shouldEqual Indexed( 1, accessChain )
    combine( openIndexed, SelfReference ) shouldEqual Indexed( 1, SelfReference )
  }

  "Combining a non-empty description with an AccessChain" should "add the description to the tail of the chain" in {
    val prefix = Generic( "prefix" )
    combine( explicit, AccessChain( prefix ) ) shouldEqual AccessChain( prefix, explicit )
    combine( generic, AccessChain( prefix ) ) shouldEqual AccessChain( prefix, generic )
    combine( materializedIndexed, AccessChain( prefix ) ) shouldEqual AccessChain( prefix, materializedIndexed )
  }

  "Combining SelfReference with any non-Empty description" should "return the description as-is" in {
    combine( SelfReference, SelfReference ) shouldEqual SelfReference
    combine( SelfReference, explicit ) shouldEqual explicit
    combine( SelfReference, generic ) shouldEqual generic
    combine( SelfReference, accessChain ) shouldEqual accessChain
    combine( SelfReference, openIndexed ) shouldEqual openIndexed
    combine( SelfReference, materializedIndexed ) shouldEqual materializedIndexed
  }

  "SelfReference" should "fail to combine with Empty descriptions" in {
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( SelfReference, Empty ) }
  }

  "Combining any other description with a SelfReference" should "return the description as-is" in {
    combine( SelfReference, SelfReference ) shouldEqual SelfReference
    combine( explicit, SelfReference ) shouldEqual explicit
    combine( generic, SelfReference ) shouldEqual generic
    combine( accessChain, SelfReference ) shouldEqual accessChain
    combine( materializedIndexed, SelfReference ) shouldEqual materializedIndexed
  }

  "Explicit description" should "override preexisting Explicit descriptions" in {
    combine( Explicit( "prior" ), explicit ) shouldEqual explicit
  }

  "Explicit description" should "fail to combine with any other description" in {
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( explicit, Empty ) }
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( explicit, generic ) }
  }

  "Generic description" should "fail to combine with any other description" in {
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( generic, Empty ) }
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( generic, explicit ) }
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( generic, generic ) }
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

  // Following three examples are designed to deal with issue #86 without introducing additional complexity to the
  // domain model. I'm not entirely happy with this solution (it feels unclean) but it'll get us through the immediate
  // future while we gain better understanding of the desirable domain model.

  "Combining an AccessChain with an open Indexed" should "produce a new AccessChain with an open Indexed at head" in {
    val a = Generic( "a" )
    combine( AccessChain( a ), openIndexed ) shouldEqual AccessChain( openIndexed, a )
  }
  
  "An AccessChain with an open Indexed at head" should 
    "combine with any description to produce a new AccessChain with a materialized Index at head" in {
    val a = Generic( "a" )
    val lhs = AccessChain( openIndexed, a )

    combine( lhs, Empty ) shouldEqual AccessChain( openIndexed.copy( of = Empty ), a )
    combine( lhs, explicit ) shouldEqual AccessChain( openIndexed.copy( of = explicit ), a )
    combine( lhs, generic ) shouldEqual AccessChain( openIndexed.copy( of = generic ), a )
    combine( lhs, accessChain ) shouldEqual AccessChain( openIndexed.copy( of = accessChain ), a )
    combine( lhs, SelfReference ) shouldEqual AccessChain( openIndexed.copy( of = SelfReference ), a )
  }

  "An AccessChain without an open Indexed at head" should "fail to combine with any other description" in {
    val a = Generic( "a" )
    val lhs = AccessChain( a )

    an[ IllegalArgumentException ] shouldBe thrownBy { combine( lhs, Empty ) }
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( lhs, explicit ) }
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( lhs, generic ) }
  }
}
