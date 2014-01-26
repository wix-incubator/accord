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

package com.wix

import scala.annotation.implicitNotFound

/** The entry-point to the Accord library. To execute a validator, simply import it into the local scope,
  * import this package and execute `validate( objectUnderValidation )`.
  */
package object accord {
  private[ accord ] val stubValidationContext = "stub"

  /** A validator is a function `T => Result`, where `T` is the type of the object under validation
    * and Result is an instance of [[com.wix.accord.Result]].
    *
    * Implementation note: While theoretically a validator can be defined as a type alias, in practice this
    * doesn't allow to specify an error message when it's implicitly missing at the call site (see
    * [[scala.annotation.implicitNotFound]]).
    *
    * @tparam T The object type this validator operates on.
    */
  @implicitNotFound( "A validator for type ${T} not found. Did you forget to import an implicit validator for " +
                     "this type? (alternatively, if you own the code, you may want to move the validator to " +
                     "the companion object for ${T} so it's automatically imported)." )
  trait Validator[ -T ] extends ( T => Result ) {
    /** Provides a textual description of the expression being evaluated. For example, a validation rule like
      * `p.firstName is notEmpty` might have the context `firstName`. The initial value is a stub and is later
      * rewritten by the validation transform.
      * 
      * @return The textual description of the object under validation.
      */
    protected def description: String = stubValidationContext    // Rewritten by the validation macro

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

  /** Validates the specified object and returns a validation [[com.wix.accord.Result]]. An implicit
    * [[com.wix.accord.Validator]] must be in scope for this call to succeed.
    *
    * @param x The object to validate.
    * @param validator A validator for objects of type `T`.
    * @tparam T The type of the object to validate.
    * @return A [[com.wix.accord.Result]] indicating success or failure of the validation.
    */
  def validate[ T ]( x: T )( implicit validator: Validator[ T ] ) = validator( x )
}
