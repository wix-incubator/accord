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


package com.wix.accord.dsl

import com.wix.accord.Validator
import com.wix.accord.combinators.HasEmpty
import com.wix.accord.combinators.Empty
import com.wix.accord.combinators.NotEmpty
import com.wix.accord.combinators.Size

trait CollectionOps {
  /** Specifies a validator that succeeds on empty instances; the object under validation must implement
    * `def isEmpty: Boolean` (see [[com.wix.accord.combinators.HasEmpty]]).
    */
  def empty[ T <% HasEmpty ]: Validator[ T ] = new Empty[ T ]

  /** Specifies a validator that fails on empty instances; the object under validation must implement
    * `def isEmpty: Boolean` (see [[com.wix.accord.combinators.HasEmpty]]).
    */
  def notEmpty[ T <% HasEmpty ]: Validator[ T ] = new NotEmpty[ T ]

  /** Provides access to size-based validators (where the object under validation must implement
    * `def size: Int`, see [[com.wix.accord.combinators.HasSize]]). Enables syntax such as
    * `c.students has size > 0`.
    */
  def size[ T ] = new Size[ T ]
}
