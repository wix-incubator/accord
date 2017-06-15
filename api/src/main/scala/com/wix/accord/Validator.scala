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

import scala.annotation.implicitNotFound

/** A validator over some type `T`.
  *
  * ==Overview==
  * An Accord validator is a function `T => Result`, where `T` is the type of the object under validation
  * and Result is an instance of [[com.wix.accord.Result]].
  *
  * Implementation note: While theoretically a validator can be defined as a type alias, in practice this
  * doesn't allow to specify an error message when it's implicitly missing at the call site.
  *
  * @tparam T The object type this validator operates on.
  */
@implicitNotFound(
  "A validator for type ${T} not found. Did you forget to import an implicit validator for " +
  "this type? (If you own the code, you may want to move the validator to the companion object " +
  "for ${T} so it's automatically included in the implicit search scope)." )
trait Validator[ -T ] extends ( T => Result ) {
  self =>

  /** Adapts this validator to a type `U`. Each application of the new validator applies the the specified
    * extractor function, and validates the resulting `T` via this validator. This enables explicit validator
    * composition, which is especially useful for defining new, complex combinators. At the validator definition
    * site, it is recommended to use the `valid` operation provided by the DSL instead.
    *
    * Important note: the specified extractor function must be able to safely handle nulls!
    *
    * @param g An extractor function from `U => T`.
    * @tparam U The target type of the adaption.
    * @return An adapted validator over type `U`.
    */
  override def compose[ U ]( g: U => T ): Validator[ U ] = new Validator[ U ] {
    override def apply( v1: U ): Result = self apply g( v1 )
  }
}

/** A companion object mostly responsible for allowing null-safe validation of boxed Java primitive types. */
object Validator {
  /** The default failure when validating `null`. */
  val nullFailure = Failure( Set( RuleViolation( null, "is a null" ) ) )

  // Primitive promotions --

  abstract class PromotedPrimitiveValidator[ U, B ]( validator: Validator[ U ] )( implicit unbox: B => U ) {
    /** Transforms this validator to a null-safe variant over the reference type. */
    def boxed = new Validator[ B ] {
      def apply( v: B ): Result = if ( v == null ) nullFailure else validator( v )
    }
  }

  implicit class PromotedByteValidator  ( v: Validator[ Byte    ] ) extends PromotedPrimitiveValidator[ Byte,    java.lang.Byte      ]( v )
  implicit class PromoteCharValidator   ( v: Validator[ Char    ] ) extends PromotedPrimitiveValidator[ Char,    java.lang.Character ]( v )
  implicit class PromoteIntValidator    ( v: Validator[ Int     ] ) extends PromotedPrimitiveValidator[ Int,     java.lang.Integer   ]( v )
  implicit class PromoteLongValidator   ( v: Validator[ Long    ] ) extends PromotedPrimitiveValidator[ Long,    java.lang.Long      ]( v )
  implicit class PromoteFloatValidator  ( v: Validator[ Float   ] ) extends PromotedPrimitiveValidator[ Float,   java.lang.Float     ]( v )
  implicit class PromoteDoubleValidator ( v: Validator[ Double  ] ) extends PromotedPrimitiveValidator[ Double,  java.lang.Double    ]( v )
  implicit class PromoteBooleanValidator( v: Validator[ Boolean ] ) extends PromotedPrimitiveValidator[ Boolean, java.lang.Boolean   ]( v )
}
