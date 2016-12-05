/*
  Copyright 2013-2015 Wix.com

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

package com.wix.accord.java8

import java.time.temporal.Temporal

import com.wix.accord.NullSafeValidator
import com.wix.accord.ViolationBuilder._

trait TemporalCombinators {

  // Annoying conversion infrastructure --
  // The following is safe because Temporal implementations must implement Comparable (per JavaDocs)
  private implicit class TemporalComparison[ T <: Temporal ]( left: T ) {
    def compareTo( right: T ): Int = left.asInstanceOf[ Comparable[ T ] ].compareTo( right )
  }
  implicit def temporalOrdering[ T <: Temporal ]: Ordering[ T ] =
    Ordering.fromLessThan { case ( l, r ) => l.asInstanceOf[ Comparable[ T ] ].compareTo( r ) < 0 }


  // Combinators --

  class Before[ T <: Temporal ]( right: T )
    extends NullSafeValidator[ T ]( _.compareTo( right ) < 0, _ -> s"must be before ${ right.toString }" )

  class After[ T <: Temporal ]( right: T )
    extends NullSafeValidator[ T ]( _.compareTo( right ) > 0, _ -> s"must be after ${ right.toString }" )

  def before[ T <: Temporal ]( right: T ) = new Before( right )
  def after[ T <: Temporal ]( right: T ) = new After( right )

}
