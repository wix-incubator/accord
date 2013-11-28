/*
  Copyright 2013 Tomer Gabel

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

package com.tomergabel.accord.tests.dsl

import org.scalatest.{FlatSpec, Matchers}
import PrimitiveSchema._
import com.tomergabel.accord.{Implicits, ResultMatchers}

class AlternativeValidationSyntaxTests extends FlatSpec with Matchers with ResultMatchers {
  "Importing com.tomergabel.accord.Implicits" should "enable alternative validation invocation syntax" in {
    import Implicits._
    val person = Person( "Edsger", "Dijkstra" )
    person.validate should be( aSuccess )
  }
}
