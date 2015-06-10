/*
  Copyright 2013-2014 Wix.com

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

package com.wix.accord.tests.dsl

import com.wix.accord._
import com.wix.accord.scalatest.ResultMatchers
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable

class CollectionOpsTests extends WordSpec with Matchers with ResultMatchers {
  import CollectionOpsTests._

  "empty" should {
    "successfully validate a empty collection" in {
      validate( Seq.empty )( seqEmptyValidator ) should be( aSuccess )
    }
    "successfully validate a non-empty collection" in {
      validate( Seq( 1, 2, 3 ) )( seqEmptyValidator ) should be( aFailure )
    }
  }
  "notEmpty" should {
    "successfully validate a non-empty collection" in {
      validate( Seq( 1, 2, 3 ) )( seqNotEmptyValidator ) should be( aSuccess )
    }
    "successfully validate a empty collection" in {
      validate( Seq.empty )( seqNotEmptyValidator ) should be( aFailure )
    }
  }
  "size extensions" should {
    // No need to test all extensions -- these should be covered in OrderingOpsTest. We only need to test
    // one to ensure correct constraint generation.
    "generate a correctly prefixed constraint" in {
      validate( Seq.empty )( seqSizeValidator ) should
        failWith( RuleViolationMatcher( constraint = "has size 0, expected more than 0" ) )
    }
  }
}

object CollectionOpsTests {
  import dsl._

  val seqEmptyValidator = validator[ Seq[_] ] { _ is empty }
  val seqNotEmptyValidator = validator[ Seq[_] ] { _ is notEmpty }
  val seqSizeValidator = validator[ Seq[_] ] { _ has size > 0 }
  val seqEachValidator = validator[ Seq[ String ] ] { _.each is empty }
}

object NewCollectionOpsTests {
  import dsl._

  trait ArbitraryType
  object ArbitraryType { def apply() = new ArbitraryType {} }

  def visit[ T ]( coll: Traversable[ T ] )( visitor: T => Result ): Result = {
    val visited = new Validator[ T ] {
      def apply( v: T ) = visitor( v )
    }
    ( coll.each is visited )( coll )
  }
}

class NewCollectionOpsTests extends WordSpec with Matchers with ResultMatchers {
  import NewCollectionOpsTests._

  "Calling \".each\" on a Traversable" should {
    "apply subsequent validation rules to all elements" in {
      val coll = Seq.fill( 5 )( ArbitraryType.apply )
      val visited = mutable.ListBuffer.empty[ ArbitraryType ]
      visit( coll ) { elem => visited append elem; Success }
      visited should contain theSameElementsAs coll
    }

    "evaluate to Success if no elements are present" in {
      val coll = Seq.empty[ Nothing ]
      val result = visit( coll ) { _ => Success }
      result shouldBe aSuccess
    }

    "evaluate to Failure if the collection is null" in {
      val result = visit( null ) { _ => Success }
      result shouldBe aFailure
    }

    "evaluate to Success if validation succeeds on all elements" in {
      val coll = Seq.fill( 5 )( ArbitraryType )
      val result = visit( coll ) { _ => Success }
      result shouldBe aSuccess
    }

    def violationFor( elem: ArbitraryType ) = RuleViolation( elem, "element", None )
    def failureFor( elem: ArbitraryType ) = Failure( Set( violationFor( elem ) ) )
    def matcherFor( elem: ArbitraryType ) = RuleViolationMatcher( value = elem, constraint = "element" )

    "evaluate to a Failure if any element failed" in {
      val coll = Seq.fill( 5 )( ArbitraryType.apply )
      val result =
        visit( coll ) {
          case elem if elem == coll.last => failureFor( elem )
          case _ => Success
        }
      result shouldBe aFailure
    }

    "include all failed elements in the aggregated result" in {
      val coll = Seq.fill( 5 )( ArbitraryType.apply )
      val failing = coll.take( 2 )
      val result =
        visit( coll ) {
          case elem if failing contains elem => failureFor( elem )
          case _ => Success
        }

      result should failWith( failing map matcherFor :_* )
    }
  }
}