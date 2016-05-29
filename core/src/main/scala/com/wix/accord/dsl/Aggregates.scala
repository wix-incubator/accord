package com.wix.accord.dsl

import com.wix.accord.Descriptions._
import com.wix.accord.{Result, Success, Validator}

object Aggregates {

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
            val result = validator apply element applyDescription ( if ( includeIndices ) Indexed( index ) else Empty )
            aggregate = aggregate and result
            index = index + 1
          }

          aggregate
        }
    }
}

