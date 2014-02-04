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

package com.wix.accord.combinators

import com.wix.accord.{RuleViolation, Validator}

// TODO ScalaDocs
trait OrderingCombinators {
  class GreaterThan[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] ) extends Validator[ T ] {
    def apply( value: T ) =
      result( ev.gt( value, bound ), RuleViolation( value, s"$prefix $value, expected more than $bound", description ) )
  }

  class GreaterThanOrEqual[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] ) 
    extends Validator[ T ] {
    
    def apply( value: T ) =
      result( ev.gteq( value, bound ), RuleViolation( value, s"$prefix $value, expected $bound or more", description ) )
  }

  class LesserThan[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] ) extends Validator[ T ] {
    def apply( value: T ) =
      result( ev.lt( value, bound ), RuleViolation( value, s"$prefix $value, expected less than $bound", description ) )
  }

  class LesserThanOrEqual[ T ]( bound: T, prefix: String )( implicit ev: Ordering[ T ] ) 
    extends Validator[ T ] {
    
    def apply( value: T ) =
      result( ev.lteq( value, bound ), RuleViolation( value, s"$prefix $value, expected $bound or less", description ) )
  }

  class EquivalentTo[ T ]( other: T, prefix: String )( implicit ev: Ordering[ T ] ) extends Validator[ T ] {
    def apply( value: T ) =
      result( ev.equiv( value, other ), RuleViolation( value, s"$prefix $value, expected $other", description ) )
  }

  class Between[ T ]( lowerBound: T, upperBound: T, prefix: String )( implicit ev: Ordering[ T ] ) 
    extends Validator[ T ]{
    
    def apply( x: T ) =
      result( ev.gteq( x, lowerBound ) && ev.lteq( x, upperBound ),
        RuleViolation( x, s"$prefix $x, expected between $lowerBound and $upperBound", description ) )

    /** Returns a new validator with an exclusive upper bound. */
    def exclusive = new Validator[ T ] {
      def apply( x: T ) =
        result( ev.gteq( x, lowerBound ) && ev.lt( x, upperBound ),
          RuleViolation( x, s"$prefix $x, expected between $lowerBound and (exclusive) $upperBound", description ) )
    }
  }
}
