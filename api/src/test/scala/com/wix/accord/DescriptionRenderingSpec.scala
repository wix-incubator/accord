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

class DescriptionRenderingSpec extends FlatSpec with Matchers {
  import Descriptions._

  "Rendering an empty description" should "result in the string \"unknown\"" in {
    render( Empty ) shouldEqual "unknown"
  }

  "Rendering an explicit description" should "result in the explicit description as-is" in {
    render( Explicit( "test" ) ) shouldEqual "test"
  }

  "Rendering a generic description" should "result in the generic description as-is" in {
    render( Generic( "test" ) ) shouldEqual "test"
  }

  "Rendering an access chain" should "result in a Scala-style indirection chain with dot separation" in {
    val chain = AccessChain( Generic( "a" ), Generic( "b" ), Generic( "c" ) )
    render( chain ) shouldEqual chain.elements.map( Descriptions.render ).mkString( "." )
  }

  "Rendering a self-reference" should "result in the string \"value\"" in {
    render( SelfReference ) shouldEqual "value"
  }

  "Rendering an indexed description" should "correctly delegate rendering of the target" in {
    val target = Generic( "test" )
    val targetDescription = Descriptions.render( target )
    render( Indexed( 5L, target ) ) should startWith( targetDescription )
  }

  "Rendering an indexed description" should "include a suffix with the index" in {
    render( Indexed( 5L, Empty ) ) should endWith( " [at index 5]" )
  }

  "Rendering a conditional description" should "correctly delegate rendering of the target" in {
    val target = Generic( "test" )
    val targetDescription = Descriptions.render( target )
    render( Conditional( on = Empty, value = 5, guard = None, target = target ) ) should startWith( targetDescription )
  }

  "Rendering a conditional description" should "include the conditional and description in the suffix" in {
    val cond = Generic( "cond" )
    val condDescription = Descriptions.render( cond )
    val value = 5
    render( Conditional( on = cond, value = value, guard = None, target = Empty ) ) should
      endWith( s" [where $condDescription=$value]" )
  }

  "Rendering a guarded conditional description" should "include the guard description in the suffix" in {
    val guard = Generic( "guard" )
    val guardDescription = Descriptions.render( guard )
    render( Conditional( on = Empty, value = 5, guard = Some( guard ), target = Empty ) ) should
      endWith( s" and $guardDescription]" )
  }
}
