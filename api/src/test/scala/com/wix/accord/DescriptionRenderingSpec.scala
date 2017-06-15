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

class DescriptionRenderingSpec extends FlatSpec with Matchers {
  import Descriptions._

  "Rendering a path" should "result in a Scala-style indirection chain with dot separation" in {
    val chain = Path( Generic( "a" ), Generic( "b" ), Generic( "c" ) )
    render( chain ) shouldEqual "a.b.c"
  }

  "Associative path elements" should "render as suffixes to their predecessor, separated by a space" in {
    val chain = Path( Generic( "a" ), Indexed( 5L ), Generic( "b" ) )
    render( chain ) shouldEqual "a [at index 5].b"
  }

  "Rendering an explicit description" should "result in the explicit description as-is" in {
    render( Explicit( "test" ) ) shouldEqual "test"
  }

  "Rendering a generic description" should "result in the generic description as-is" in {
    render( Generic( "test" ) ) shouldEqual "test"
  }

  "Rendering a self-reference" should "result in the string \"value\"" in {
    render( SelfReference ) shouldEqual "value"
  }

  "Rendering an indexed description" should "result in the index suffix" in {
    render( Indexed( 5L ) ) shouldEqual "[at index 5]"
  }

  "Rendering a conditional description" should "include the predicated object in the result, if specified" in {
    val cond = Path( Generic( "cond" ) )
    val condDescription = Descriptions.render( cond )
    val value = 5
    val conditional = Conditional( on = cond, value = value, guard = None )
    render( conditional ) shouldEqual s"[where $condDescription=$value]"
  }

  "Rendering a conditional description" should "include the guard in the result, if specified" in {
    val guard = Generic( "guard" )
    val guardDescription = Descriptions.render( guard )
    render( Conditional( on = Path.empty, value = 5, guard = Some( guard ) ) ) shouldEqual
      s"[where $guardDescription]"
  }

  "Rendering a conditional description" should "include both predicated object and guard, if both are specified" in {
    val cond = Path( Generic( "cond" ) )
    val condDescription = Descriptions.render( cond )
    val value = 5
    val guard = Generic( "guard" )
    val guardDescription = Descriptions.render( guard )
    val conditional = Conditional( on = cond, value = value, guard = Some( guard ) )
    render( conditional ) shouldEqual s" [where $condDescription=$value and $guardDescription]"
  }
}
