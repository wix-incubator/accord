package com.wix.accord.dsl

import com.wix.accord.Validator
import com.wix.accord.combinators.And
import com.wix.accord.combinators.Or
import com.wix.accord.combinators.Valid
import com.wix.accord.combinators.IsNull
import com.wix.accord.combinators.IsNotNull
import com.wix.accord.combinators.EqualTo
import com.wix.accord.combinators.NotEqualTo

trait GenericOps {
  /** Extends validators with useful helpers.
    *
    * @param validator The validator to be extended.
    * @tparam T The type of the object under validation.
    */
  implicit class ValidatorBooleanOps[ T ]( validator: Validator[ T ] ) {
    def and( other: Validator[ T ] ) = new And( validator, other ) // TODO shortcut multiple ANDs
    def or( other: Validator[ T ] ) = new Or( validator, other )   // TODO shortcut multiple ORs
  }


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
