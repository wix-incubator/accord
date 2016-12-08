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

/** Provides alternative syntax for validation. Instead of having to explicitly call
  * [[com.wix.accord.validate]], an object can be validated by calling
  * `instanceUnderTest.validate`. This is strictly an aesthetic preference, there are
  * no differences in implementation or execution.
  */
object Implicits {
  implicit class ExtendObjectForValidation[ T : Validator ]( x: T ) {
    /** Executes the validation rule for this instance.
      *
      * @return A [[com.wix.accord.Result]] indicating whether or not validation
      *         was successful.
      */
    def validate: Result = implicitly[ Validator[ T ] ].apply( x )
  }
}
