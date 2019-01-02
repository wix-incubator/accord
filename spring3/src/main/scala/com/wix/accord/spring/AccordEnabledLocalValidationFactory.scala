/*
  Copyright 2013-2019 Wix.com

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

import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.validation.Errors
import scala.reflect.ClassTag
import javax.annotation.Resource

class AccordEnabledLocalValidationFactory extends LocalValidatorFactoryBean with SpringAdapterBase {

  @Resource private val resolver: AccordValidatorResolver = null

  // Need an indirection through a separate method to get a type T in a way that's consistently usable
  // across multiple expressions. There might be a better way to do this.
  private def internalValidate[ T : ClassTag ]( target: T, errors: Errors ): Unit = {
    resolver.lookupValidator[ T ] match {
      case Some( validator ) => applyAdaptedValidator( validator, target, errors )
      case None => super.validate( target, errors )
    }
  }

  override def validate( target: Any, errors: Errors ): Unit = {
    internalValidate( target, errors )( ClassTag( target.getClass ) )
  }

  override def validate( target: Any, errors: Errors, validationHints: AnyRef* ): Unit = {
    // TODO is this sane?...
    super.validate( target, errors, validationHints )
  }
}
