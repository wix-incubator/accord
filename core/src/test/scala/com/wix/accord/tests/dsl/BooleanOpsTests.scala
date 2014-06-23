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
  
  "and" should {
    "succeed when both sides of the operator validate successfully" in {
      validate( CompoundTest( left = true, right = true ) )( andValidator ) should be( aSuccess )
    }
    "fail when the left side of the operator fails to validate" in pendingUntilFixed {              // Issue #23
      validate( CompoundTest( left = false, right = true ) )( andValidator ) should be( aFailure )
    }
    "fail when the right side of the operator fails to validate" in {
      validate( CompoundTest( left = true, right = false ) )( andValidator ) should be( aFailure )
    }
    "fail when both sides of the operator fail to validate" in {
      validate( CompoundTest( left = false, right = false ) )( andValidator ) should be( aFailure )
    }
  }

  "or" should {
    "succeed when both sides of the operator validate successfully" in {
      validate( CompoundTest( left = true, right = true ) )( orValidator ) should be( aSuccess )
    }
    "succeed when only the left side of the operator validates successfully" in pendingUntilFixed {  // Issue #23
      validate( CompoundTest( left = true, right = false ) )( orValidator ) should be( aSuccess )
    }
    "succeed when only the right side of the operator validates successfully" in {
      validate( CompoundTest( left = false, right = true ) )( orValidator ) should be( aSuccess )
    }
    "fail when both sides of the operator fail to validate" in {
      validate( CompoundTest( left = false, right = false ) )( orValidator ) should be( aFailure )
    }
  }
}

object BooleanOpsTests {
  case class SimpleTest( f: Boolean )
  case class CompoundTest( left: Boolean, right: Boolean )

  import com.wix.accord.dsl._
  val  trueValidator = validator[ SimpleTest ] { _.f is true }
  val falseValidator = validator[ SimpleTest ] { _.f is false }
  val   andValidator = validator[ CompoundTest ] { c => ( c.left is true ) and ( c.right is true ) }
  val    orValidator = validator[ CompoundTest ] { c => ( c.left is true ) or  ( c.right is true ) }
}