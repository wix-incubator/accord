package com.wix.accord.dsl

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
          val appendIndex: ( Option[ String ] => Option[ String ] ) =
            if ( includeIndices ) { prefix: Option[ String ] => Some( prefix.getOrElse( "" ) + s" [at index $index]" ) }
            else identity

          var aggregate: Result = Success
          coll foreach { element =>
            val result = validator apply element withDescription appendIndex
            aggregate = aggregate and result
            index = index + 1
          }

          aggregate
        }
    }
}

