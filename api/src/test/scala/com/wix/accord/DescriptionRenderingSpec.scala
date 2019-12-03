/*
  Copyright 2013-2019 Wix.com

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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DescriptionRenderingSpec extends AnyFlatSpec with Matchers {
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

  "Rendering a self-reference (empty path)" should "result in the string \"value\"" in {
    render( Path.empty ) shouldEqual "value"
  }

  "Rendering an indexed description" should "result in the index suffix" in {
    render( Indexed( 5L ) ) shouldEqual "[at index 5]"
  }

  "Rendering a branch" should "result in a branch marker for the true branch" in {
    val guard = Generic( "guard" )
    val guardDescription = Descriptions.render( guard )
    val branch = Branch( guard, evaluation = true )
    render( branch ) shouldEqual s"[where $guardDescription is true]"
  }

  "Rendering a branch" should "result in a branch marker for the false branch" in {
    val guard = Generic( "guard" )
    val guardDescription = Descriptions.render( guard )
    val branch = Branch( guard, evaluation = false )
    render( branch ) shouldEqual s"[where $guardDescription is false]"
  }

  "Rendering a simple pattern match" should "result in a value marker for the selected pattern" in {
    val target = Path( Generic( "target" ) )
    val targetDescription = render( target )
    val patternMatch = PatternMatch( target, 5, None )
    render( patternMatch ) shouldEqual s"[where $targetDescription=5]"
  }

  "Rendering a guarded pattern match" should "include the guard in the result" in {
    val target = Path( Generic( "target" ) )
    val targetDescription = render( target )
    val guard = Generic( "someProperty == 2" )
    val guardDescription = render( guard )
    val patternMatch = PatternMatch( target, 5, Some( guard ) )
    render( patternMatch ) shouldEqual s"[where $targetDescription=5 and $guardDescription]"
  }
}
