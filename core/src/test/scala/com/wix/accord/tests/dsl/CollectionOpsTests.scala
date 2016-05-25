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

package com.wix.accord.tests.dsl

import com.wix.accord.Descriptions.Generic
import com.wix.accord._
import com.wix.accord.scalatest.ResultMatchers
import org.scalatest.{Inside, Matchers, WordSpec}

import scala.collection.mutable

object CollectionOpsTests {
  import dsl._

  trait ArbitraryType
  object ArbitraryType { def apply() = new ArbitraryType {} }

  private val someSeq = Seq.empty[ ArbitraryType ]
  val emptyValidator = someSeq is empty
  val notEmptyValidator = someSeq is notEmpty
  val distinctValidator = someSeq is distinct
  val inItemsValidator = 1 is in( 1, 3, 5 )
  val inSetValidator = 1 is in( Set( 1, 3, 5 ) )

  class Sized( _size: Int ) {
    private var _visited = false
    def size: Int = { _visited = true; _size }
    def visited = _visited
  }
  val sizeValidator = new Sized( 0 ) has size > 5

  import scala.language.implicitConversions
  private implicit def visitorToValidator[ T ]( visitor: T => Result ): Validator[ T ] =
    new Validator[ T ] { def apply( v: T ) = visitor( v ) }

  def visitEach[ T ]( seq: Seq[ T ] )( visited: T => Result ): Result = ( seq.each is visited )( seq )
  def visitEach[ T ]( opt: Option[ T ] )( visited: T => Result ): Result = ( opt.each is visited )( opt )
  def visitEach[ T ]( set: Set[ T ] )( visited: T => Result ): Result = ( set.each is visited )( set )
}

class CollectionOpsTests extends WordSpec with Matchers with ResultMatchers with Inside {
  import CollectionOpsTests._
  import combinators.{Empty, NotEmpty, Distinct,In}


  "Calling \"has size\"" should {
    "fail to compile on a type with no \"size\" property" in {
      """
        import com.wix.accord.dsl._
        val lhs: Int = 0
        lhs has com.wix.accord.dsl.size > 5
      """ shouldNot compile
    }

    "apply subsequent validation rules to the \"size\" property of an object" in {
      val ouv = new Sized( 10 )
      sizeValidator( ouv )
      ouv.visited shouldBe true
    }
  }

  "The expression \"is empty\"" should {
    "return an Empty combinator" in {
      emptyValidator shouldBe an[ Empty[_] ]
    }
  }

  "The expression \"is notEmpty\"" should {
    "return an Empty combinator" in {
      notEmptyValidator shouldBe a[ NotEmpty[_] ]
    }
  }

  "The expression \"is distinct\"" should {
    "return the Distinct combinator" in {
      distinctValidator shouldBe a[ Distinct.type ]
    }
  }

  "The expression \"in\"" should {
    "accept a set and return a suitably-configured In combinator" in {
      inSetValidator shouldEqual In( Set( 1, 3, 5 ), "got" )
    }

    "accept a varags item list and return a suitably-configured In combinator" in {
      inItemsValidator shouldEqual In( Set( 1, 3, 5 ), "got" )
    }
  }

  "Calling \".each\" on a Traversable" should {
    "apply subsequent validation rules to all elements" in {
      val coll = Seq.fill( 5 )( ArbitraryType.apply )
      val visited = mutable.ListBuffer.empty[ ArbitraryType ]
      visitEach( coll ) { elem => visited append elem; Success }
      visited should contain theSameElementsAs coll
    }

    "evaluate to Success if no elements are present" in {
      val coll = Seq.empty[ Nothing ]
      val result = visitEach( coll ) { _ => Success }
      result shouldBe aSuccess
    }

    "evaluate to Failure if the collection is null" in {
      val seq: Seq[ ArbitraryType ] = null
      val result = visitEach( seq ) { _ => Success }
      result shouldBe aFailure
    }

    "evaluate to Success if validation succeeds on all elements" in {
      val coll = Seq.fill( 5 )( ArbitraryType )
      val result = visitEach( coll ) { _ => Success }
      result shouldBe aSuccess
    }

    def violationFor( elem: ArbitraryType ) = RuleViolation( elem, "fake constraint", Generic( "failure" ) )
    def failureFor( elem: ArbitraryType ) = Failure( Set( violationFor( elem ) ) )
    def matcherFor( elem: ArbitraryType ) = RuleViolationMatcher( value = elem, constraint = "fake constraint" )

    "evaluate to a Failure if any element failed" in {
      val coll = Seq.fill( 5 )( ArbitraryType.apply )
      val result =
        visitEach( coll ) {
          case elem if elem == coll.last => failureFor( elem )
          case _ => Success
        }
      result shouldBe aFailure
    }

    "include all failed elements in the aggregated result" in {
      val coll = Seq.fill( 5 )( ArbitraryType.apply )
      val failing = coll.take( 2 )
      val result =
        visitEach( coll ) {
          case elem if failing contains elem => failureFor( elem )
          case _ => Success
        }

      result should failWith( failing map matcherFor :_* )
    }

    "include position in a failed element's description for sequences" in {
      val coll = Seq.fill( 5 )( ArbitraryType.apply )
      val result =
        visitEach( coll ) {
          case elem if elem == coll( 2 ) => failureFor( elem )
          case _ => Success
        }

      result should failWith( RuleViolationMatcher( value = coll( 2 ), description = "failure [at index 2]" ) )
    }

    "not include positional information for options" in {
      val coll = Option( ArbitraryType.apply )
      val result = visitEach( coll ) { failureFor(_) }
      result should failWith( RuleViolationMatcher( description = "failure" ) )
    }

    "not include positional information for sets" in {
      val coll = Set( ArbitraryType.apply )
      val result = visitEach( coll ) { failureFor(_) }
      result should failWith( RuleViolationMatcher( description = "failure" ) )
    }
  }
}