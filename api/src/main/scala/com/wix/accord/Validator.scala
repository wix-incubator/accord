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

package com.wix.accord

import scala.annotation.implicitNotFound

/** A validator is a function `T => Result`, where `T` is the type of the object under validation
  * and Result is an instance of [[com.wix.accord.Result]].
  *
  * Implementation note: While theoretically a validator can be defined as a type alias, in practice this
  * doesn't allow to specify an error message when it's implicitly missing at the call site (see
  * [[scala.annotation.implicitNotFound]]).
  *
  * @tparam T The object type this validator operates on.
  */
@implicitNotFound(
  "A validator for type ${T} not found. Did you forget to import an implicit validator for " +
  "this type? (alternatively, if you own the code, you may want to move the validator to " +
  "the companion object for ${T} so it's automatically imported)." )
trait Validator[ -T, +C ] extends ( T => Result[ C ] ) {
  self =>

  /** Adapts this validator to a type `U`. Each application of the new validator applies the the specified
    * extractor function, and validates the resulting `T` via this validator. This enables explicit validator
    * composition, which is especially useful for defining new, complex combinators. At the validator definition
    * site, it is recommended to use the `valid` operation provided by the DSL instead.
    *
    * Important note: since there is no way to enforce null-safety at the type system level, the specified extractor
    * function must be able to safely handle nulls.
    *
    * @param g An extractor function from `U => T`.
    * @tparam U The target type of the adaption.
    * @return An adapted validator of `U`.
    */
  override def compose[ U ]( g: U => T ): Validator[ U, C ] = new Validator[ U, C ] {
    override def apply( v1: U ): Result[ C ] = self apply g( v1 )
  }
}

trait ResultModel {
  type Constraint
  def nullConstraint: Constraint

  type Validator[ -T ] = com.wix.accord.Validator[ T, Constraint ]
  type Violation = com.wix.accord.Violation[ Constraint ]
  type RuleViolation = com.wix.accord.RuleViolation[ Constraint ]
  val RuleViolation = com.wix.accord.RuleViolation
  type GroupViolation = com.wix.accord.GroupViolation[ Constraint ]
  val GroupViolation = com.wix.accord.GroupViolation
  type Failure = com.wix.accord.Failure[ Constraint ]
  val Success = com.wix.accord.Success
  val Failure = com.wix.accord.Failure
  type Result = com.wix.accord.Result[ Constraint ]
}

object Validator {
  /** The default failure for null validations. */
  // is a null
  def nullFailure( implicit model: ResultModel ): Failure[ model.Constraint ] =
    Failure( Set( RuleViolation( null, model.nullConstraint, None ) ) )
//
  abstract class PromotedPrimitiveValidator[ U, B, R <: ResultModel ]( validator: Validator[ U, R#Constraint ] )( implicit unbox: B => U, model: R ) {
    /** Transforms this validator to a null-safe variant over the reference type. */
    def boxed = new Validator[ B, R#Constraint ] {
      def apply( v: B ) = if ( v == null ) nullFailure else validator( v )
    }
  }

  implicit class PromotedByteValidator[ R <: ResultModel ]( v: Validator[ Byte, R#Constraint ] )( implicit model: R )
    extends PromotedPrimitiveValidator[ Byte, java.lang.Byte, R ]( v )
  implicit class PromoteCharValidator[ R <: ResultModel ]( v: Validator[ Char, R#Constraint ] )( implicit model: R )
    extends PromotedPrimitiveValidator[ Char, java.lang.Character, R ]( v )
  implicit class PromoteIntValidator[ R <: ResultModel ]( v: Validator[ Int, R#Constraint ] )( implicit model: R )
    extends PromotedPrimitiveValidator[ Int, java.lang.Integer, R ]( v )
  implicit class PromoteLongValidator[ R <: ResultModel ]( v: Validator[ Long, R#Constraint ] )( implicit model: R )
    extends PromotedPrimitiveValidator[ Long, java.lang.Long, R ]( v )
  implicit class PromoteFloatValidator[ R <: ResultModel ]( v: Validator[ Float, R#Constraint ] )( implicit model: R )
    extends PromotedPrimitiveValidator[ Float, java.lang.Float, R ]( v )
  implicit class PromoteDoubleValidator[ R <: ResultModel ]( v: Validator[ Double, R#Constraint ] )( implicit model: R )
    extends PromotedPrimitiveValidator[ Double, java.lang.Double, R ]( v )
  implicit class PromoteBooleanValidator[ R <: ResultModel ]( v: Validator[ Boolean, R#Constraint ] )( implicit model: R )
    extends PromotedPrimitiveValidator[ Boolean, java.lang.Boolean, R ]( v )
}
