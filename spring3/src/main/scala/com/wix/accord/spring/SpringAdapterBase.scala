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

package com.wix.accord.spring

import com.wix.accord._
import org.springframework.validation.Errors

trait SpringAdapterBase {

  /** Formats Spring Validation rejection messages. */
  protected def formatMessage( failure: Violation ) =
    s"${Descriptions.render( failure.description )} ${failure.constraint}"

  /** Formats Spring Validation error codes. Currently hardcoded. */
  protected def formatErrorCode( failure: Violation ) = SpringAdapterBase.defaultErrorCode

  /** Adapts the specified Accord validator and applies it to the specified [[org.springframework.validation.Errors]]
    * object.
    */
  protected def applyAdaptedValidator[ T ]( validator: Validator[ T ], target: T, errors: Errors ) {
    validator( target ) match {
      case Failure( violations ) =>
        violations foreach { v => errors.reject( formatErrorCode( v ), formatMessage( v ) ) }
      case Success =>
        // Do nothing
    }
  }
}

object SpringAdapterBase {
  val defaultErrorCode = "accord.validation.failure"
}
