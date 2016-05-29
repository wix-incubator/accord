package com.wix.accord

import org.scalatest.{FlatSpec, Matchers}

class DescriptionModelSpec extends FlatSpec with Matchers {
  import Descriptions._

  private val indexed = Indexed( 1, Empty )
  private val explicit = Explicit( "test" )
  private val generic = Generic( "test" )
  private val accessChain = AccessChain( "a", "b", "c" )

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

  "Combining an AccessChain with another AccessChain" should "produce a new AccessChain indirecting left-to-right" in {
    combine( AccessChain( "a" ), AccessChain( "b" ) ) shouldEqual AccessChain( "a", "b" )
  }

  "Explicit description" should "combine with nothing" in {
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( explicit, generic ) }
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( explicit, explicit ) }
  }
}