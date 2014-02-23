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
trait Validator[ -T ] extends ( T => Result ) {
  self =>

  /** Adapts this validator to a type `U`. Each application of the new validator applies the the specified
    * extractor function, and validates the resulting `T` via this validator. This enables explicit validator
    * composition, which is especially useful for defining new, complex combinators. At the validator definition
    * site, it is recommended to use the `valid` operation provided by the DSL instead.
    *
    * @param g An extractor function from `U => T`.
    * @tparam U The target type of the adaption.
    * @return An adapted validator of `U`.
    */
  override def compose[ U ]( g: U => T ): Validator[ U ] = new Validator[ U ] {
    override def apply( v1: U ): Result = self apply g( v1 )
  }
}

/** A convenience base trait for validator definition, providing the `result` method and a DSL for constructing
  * violations (see [[com.wix.accord.ViolationBuilder]] for details).
  *
  * @tparam T The object type this validator operates on.
  */
trait BaseValidator[ T ] extends Validator[ T ] with ViolationBuilder {
  /** A helper method to simplify rendering results.
    *
    * @param test The validation test. If it succeeds, [[com.wix.accord.Success]] is returned, otherwise
    *             a [[com.wix.accord.Failure]] is generated based on the specified violation generator.
    * @param violation A generator for a validation violation. Only called if the test fails.
    * @return A [[com.wix.accord.Result]] instance with the results of the validation.
    */
  protected def result( test: => Boolean, violation: => Violation ) =
    if ( test ) Success else Failure( Seq( violation ) )
}
