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

package com.wix.accord.dsl

import com.wix.accord.Validator
import com.wix.accord.combinators.And
import com.wix.accord.combinators.Or
import com.wix.accord.combinators.Valid
import com.wix.accord.combinators.IsNull
import com.wix.accord.combinators.IsNotNull
import com.wix.accord.combinators.EqualTo
import com.wix.accord.combinators.NotEqualTo

/** Provides a DSL for untyped validators. */
trait GenericOps {
  /** Delegates validation to a pre-defined validation rule, which is encoded as an implicit
    * [[com.wix.accord.Validator]] in scope. Enables composition of validation rules, as in:
    *
    * ```
    * case class Address( address1: String, address2: String, city: String, ... )
    * case class Item( sku: String, count: Int, ... )
    * case class Shipment( items: Seq[ Item ], address: Location, ... )
    *
    * implicit val addressValidator = validator[ Address ] { ... }
    * implicit val itemValidator = validator[ Item ] { ... }
    *
    * implicit val shipmentValidator = validator[ Shipment ] { shipment =>
    *   shipment.address is valid       // Implicitly uses addressValidator
    *   shipment.items.each is valid    // Implicitly uses itemValidator
    * }
    *
    * ```
    */
  def valid[ T ]( implicit validator: Validator[ T ] ): Validator[ T ] = new Valid[ T ]

  /** Specifies a validator that succeeds only if the validation expression is null. */
  def aNull: Validator[ AnyRef ] = new IsNull

  /** Specifies a validator that succeeds only if the validation expression is not null. */
  def notNull: Validator[ AnyRef ] = new IsNotNull

  /** Specifies a validator that succeeds only if the validation expression is equal to the specified value. Respects
    * nulls an performs equality checks via [[java.lang.Object.equals]].
    */
  def equalTo[ T ]( to: T ): Validator[ T ] = new EqualTo[ T ]( to )

  /** Specifies a validator that succeeds only if the validation expression is not equal to the specified value.
    * Respects nulls an performs equality checks via [[java.lang.Object.equals]].
    */
  def notEqualTo[ T ]( to: T ): Validator[ T ] = new NotEqualTo[ T ]( to )
}
