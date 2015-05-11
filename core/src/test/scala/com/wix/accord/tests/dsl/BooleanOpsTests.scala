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

import com.wix.accord.scalatest.ResultMatchers
import org.scalatest.{WordSpec, Matchers}
import com.wix.accord._

class BooleanOpsTests extends WordSpec with Matchers with ResultMatchers {
  import BooleanOpsTests._

  "true" should {
    "succeed on true" in {
      validate( SimpleTest( f = true ) )( trueValidator ) should be( aSuccess )
    }
    "fail on false" in {
      validate( SimpleTest( f = false ) )( trueValidator ) should be( aFailure )
    }
  }

  "false" should {
    "succeed on false" in {
      validate( SimpleTest( f = false ) )( falseValidator ) should be( aSuccess )
    }
    "fail on true" in {
      validate( SimpleTest( f = true ) )( falseValidator ) should be( aFailure )
    }
  }
  
  "and (with two clauses)" should {
    "succeed when both sides of the operator validate successfully" in {
      validate( BinaryTest( left = true, right = true ) )( andValidator ) should be( aSuccess )
    }
    "fail when the left side of the operator fails to validate" in {
      validate( BinaryTest( left = false, right = true ) )( andValidator ) should be( aFailure )
    }
    "fail when the right side of the operator fails to validate" in {
      validate( BinaryTest( left = true, right = false ) )( andValidator ) should be( aFailure )
    }
    "fail when both sides of the operator fail to validate" in {
      validate( BinaryTest( left = false, right = false ) )( andValidator ) should be( aFailure )
    }
  }

  "and (with multiple clauses)" should {
    "succeed when all clauses validate successfully" in {
      validate( TrinaryTest( c1 = true, c2 = true, c3 = true ) )( nestedAndValidator ) should be( aSuccess )
    }
    "fail when any clause fails to validate" in {
      validate( TrinaryTest( c1 = false, c2 = true,  c3 = true  ) )( nestedAndValidator ) should be( aFailure )
      validate( TrinaryTest( c1 = true,  c2 = false, c3 = true  ) )( nestedAndValidator ) should be( aFailure )
      validate( TrinaryTest( c1 = true,  c2 = true,  c3 = false ) )( nestedAndValidator ) should be( aFailure )
    }
  }

  "or (with two clauses)" should {
    "succeed when both sides of the operator validate successfully" in {
      validate( BinaryTest( left = true, right = true ) )( orValidator ) should be( aSuccess )
    }
    "succeed when only the left side of the operator validates successfully" in {
      validate( BinaryTest( left = true, right = false ) )( orValidator ) should be( aSuccess )
    }
    "succeed when only the right side of the operator validates successfully" in {
      validate( BinaryTest( left = false, right = true ) )( orValidator ) should be( aSuccess )
    }
    "fail when both sides of the operator fail to validate" in {
      validate( BinaryTest( left = false, right = false ) )( orValidator ) should be( aFailure )
    }
  }

  "or (with multiple clauses)" should {
    "fail when all clauses fail to validate" in {
      validate( TrinaryTest( c1 = false, c2 = false, c3 = false ) )( nestedOrValidator ) should be( aFailure )
    }
    "succeed when any clause validates successfully" in {
      validate( TrinaryTest( c1 = true,  c2 = false, c3 = false ) )( nestedOrValidator ) should be( aSuccess )
      validate( TrinaryTest( c1 = false, c2 = true,  c3 = false ) )( nestedOrValidator ) should be( aSuccess )
      validate( TrinaryTest( c1 = false, c2 = false, c3 = true  ) )( nestedOrValidator ) should be( aSuccess )
    }
  }

}

object BooleanOpsTests {
  case class SimpleTest( f: Boolean )
  case class BinaryTest( left: Boolean, right: Boolean )
  case class TrinaryTest( c1: Boolean, c2: Boolean, c3: Boolean )

  import com.wix.accord.dsl._
  val      trueValidator = validator[ SimpleTest ] { _.f is true }
  val     falseValidator = validator[ SimpleTest ] { _.f is false }
  val       andValidator = validator[ BinaryTest ] { b => ( b.left is true ) and ( b.right is true ) }
  val        orValidator = validator[ BinaryTest ] { b => ( b.left is true ) or  ( b.right is true ) }
  val nestedAndValidator = validator[ TrinaryTest ] { t => ( t.c1 is true ) and ( t.c2 is true ) and ( t.c3 is true ) }
  val  nestedOrValidator = validator[ TrinaryTest ] { t => ( t.c1 is true ) or  ( t.c2 is true ) or  ( t.c3 is true ) }

  // While the following does not feature in any of the spec examples, it's intended to prove that
  // heterogeneous types can be used with boolean combinators.
  case class HeterogeneousTypeTest( f1: Boolean, f2: String )
  val hgTypeValidator = validator[ HeterogeneousTypeTest ] { hg => ( hg.f1 is false ) or ( hg.f2 is notEmpty ) }
}