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

package com.wix

/** The entry-point to the Accord library. To execute a validator, simply import it into the local scope,
  * import this package and execute `validate( objectUnderValidation )`.
  */
package object accord {
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
