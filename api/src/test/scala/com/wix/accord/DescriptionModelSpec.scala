package com.wix.accord

import org.scalatest.{FlatSpec, Matchers}

class DescriptionModelSpec extends FlatSpec with Matchers {
  import Descriptions._

  private val empty = Empty
  private val indexed = Indexed( 1, empty )
  private val explicit = Explicit( "test" )
  private val generic = Generic( "test" )
  private val accessChain = AccessChain( Seq( "a", "b", "c" ) )
  private val selfReference = SelfReference

  "Any combination of Empty" should "return the other description as-is" in {
    combine( empty, empty ) shouldEqual empty
    combine( empty, indexed ) shouldEqual indexed
    combine( empty, explicit ) shouldEqual explicit
    combine( empty, generic ) shouldEqual generic
    combine( empty, accessChain ) shouldEqual accessChain
    combine( empty, selfReference ) shouldEqual selfReference
    combine( empty, empty ) shouldEqual empty
    combine( indexed, empty ) shouldEqual indexed
    combine( explicit, empty ) shouldEqual explicit
    combine( generic, empty ) shouldEqual generic
    combine( accessChain, empty ) shouldEqual accessChain
    combine( selfReference, empty ) shouldEqual selfReference
  }

  "Any combination of Indexed (of Empty)" should "replace the empty inner description with the specified one" in {
    combine( indexed, indexed ) shouldEqual Indexed( 1, indexed )
    combine( indexed, explicit ) shouldEqual Indexed( 1, explicit )
    combine( indexed, generic ) shouldEqual Indexed( 1, generic )
    combine( indexed, accessChain ) shouldEqual Indexed( 1, accessChain )
    combine( indexed, selfReference ) shouldEqual Indexed( 1, selfReference )
    combine( indexed, indexed ) shouldEqual Indexed( 1, indexed )
    combine( explicit, indexed ) shouldEqual Indexed( 1, explicit )
    combine( generic, indexed ) shouldEqual Indexed( 1, generic )
    combine( accessChain, indexed ) shouldEqual Indexed( 1, accessChain )
    combine( selfReference, indexed ) shouldEqual Indexed( 1, selfReference )
  }

  "Explicit description" should "combine with nothing" in {
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( explicit, generic ) }
    an[ IllegalArgumentException ] shouldBe thrownBy { combine( explicit, explicit ) }
  }
}