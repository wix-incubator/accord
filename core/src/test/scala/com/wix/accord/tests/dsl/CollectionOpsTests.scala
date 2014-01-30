/*
  Copyright 2013 Wix.com

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
import com.wix.accord.dsl

class CollectionOpsTests extends WordSpec with Matchers with ResultMatchers {
}

object CollectionOpsTests {
  import dsl._

  val seqEmptyValidator = validator[ Seq[_] ] { _ is empty }
  val seqNotEmptyValidator = validator[ Seq[_] ] { _ is notEmpty }
  val seqSizeValidator = validator[ Seq[_] ] { _ has size > 0 }
}
