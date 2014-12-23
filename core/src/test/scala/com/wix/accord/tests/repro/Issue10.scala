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

package com.wix.accord.tests.repro

import org.scalatest.{Matchers, FlatSpec}
import com.wix.accord._

object Issue10 {
  case class Foo( bar: Option[ String ] )

  implicit val questionValidator = validator[ Foo ] { f =>
    f.bar has size > 2
  }
}

class Issue10 extends FlatSpec with Matchers {
  import Issue10._

  "Size DSL on an option" should "not throw a StackOverflowError in runtime" in {
    val foo = Foo( Some( "bar" ) )
    validate( foo )
  }
}

