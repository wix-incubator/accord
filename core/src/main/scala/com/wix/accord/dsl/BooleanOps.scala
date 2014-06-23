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

package com.wix.accord.dsl

import com.wix.accord.Validator
import com.wix.accord.combinators.{IsTrue, IsFalse, And, Or}

/** Provides a DSL for booleans. */
trait BooleanOps {
  import scala.language.implicitConversions

  /** An implicit conversion from boolean to a respective `IsTrue`/`IsFalse` instance; this enables syntax
    * such as `customer.emailOptIn is true`.
    */
  implicit def booleanToBooleanValidator( b: Boolean ): Validator[ Boolean ] =
    if ( b ) new IsTrue else new IsFalse

  /** Extends validators with useful helpers.
    *
    * @param validator The validator to be extended.
    * @tparam T The type of the object under validation.
    */
  implicit class ValidatorBooleanOps[ T ]( validator: Validator[ T ] ) {
    def and( other: Validator[ T ] ) = new And( validator, other ) // TODO shortcut multiple ANDs
    def or( other: Validator[ T ] ) = new Or( validator, other )   // TODO shortcut multiple ORs
  }

}
