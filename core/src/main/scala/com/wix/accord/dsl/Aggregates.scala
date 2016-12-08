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

package com.wix.accord.dsl

import com.wix.accord.Descriptions._
import com.wix.accord.{Result, Success, Validator}

object Aggregates {

  /**
    * Returns a new validator that applies the specified element validation rule to all elements of a collection.
    *
    * @param includeIndices A flag indicating whether or not to include index information in generated violations.
    * @param validator A validator for each element of the collection.
    * @param ev Evidence that `Coll` is, or can be converted into, a collection.
    * @tparam Coll The type of the collection this aggregate operators over.
    * @tparam Element The type of an element within `Coll`.
    * @return A validator that executes `validator` against all elements of a collection and aggregates the result.
    */
  def all[ Coll, Element ]( includeIndices: Boolean = true )
                          ( validator: Validator[ Element ] )
                          ( implicit ev: Coll => Traversable[ Element ] ): Validator[ Coll ] =
    new Validator[ Coll ] {
      def apply( coll: Coll ): Result =
        if ( coll == null ) Validator.nullFailure
        else {
          var index = 0
          var aggregate: Result = Success

          coll foreach { element =>
            val result = if ( includeIndices )
              validator apply element applyDescription Indexed( index, Empty )
            else
              validator apply element
            aggregate = aggregate and result
            index = index + 1
          }

          aggregate
        }
    }
}

