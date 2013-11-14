/*
  Copyright 2013 Tomer Gabel

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

package com.tomergabel

/** The entry-point to the Accord library. To execute a validator, simply import it into the local scope,
  * import this package and execute `validate( objectUnderValidation )`.
  */
package object accord {
  /** A type alias. A validator is in fact a function `T => Result`, where `T` is the type of the object
    * under validation and Result is an instance of [[com.tomergabel.accord.Result]]. This alias is merely
    * a semantic label for a suitable validation function.
    */
  type Validator[ T ] = T => Result     // TODO convert to trait and add @implicitNotFound

  /** Validates the specified object and returns a validation [[com.tomergabel.accord.Result]]. An implicit
    * [[com.tomergabel.accord.Validator]] must be in scope for this call to succeed.
    *
    * @param x The object to validate
    * @param validator A validator for objects of type `T`
    * @tparam T The type of the object to validate
    * @return A [[com.tomergabel.accord.Result]] indicating success or failure of the validation.
    */
  def validate[ T ]( x: T )( implicit validator: Validator[ T ] ) = validator( x )
}
