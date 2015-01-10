package com.wix.accord.dslcontext

import com.wix.accord.Domain
import com.wix.accord.dslcontext.CollectionOps._

// TODO ScalaDocs

trait Contexts {

  protected val domain: Domain
  import domain._

  trait DslContext[ Inner, Outer ] {
    protected def transform: Validator[ Inner ] => Validator[ Outer ]

    private def aggregate[ Coll, Element ]( validator: Validator[ Element ], aggregator: Traversable[ Result ] => Result )
                                          ( implicit ev: Coll => Traversable[ Element ] ): Validator[ Coll ] =
      new Validator[ Coll ] {
        def apply( col: Coll ) = if ( col == null ) nullFailure else aggregator( col map validator )
      }

    protected def all[ Coll, Element ]( validator: Validator[ Element ] )
                                      ( implicit ev: Coll => Traversable[ Element ] ): Validator[ Coll ] =
      aggregate( validator, r => ( r fold Success )( _ and _ ) )

    def is    ( validator: Validator[ Inner ] ): Validator[ Outer ] = transform apply validator
    def should( validator: Validator[ Inner ] ): Validator[ Outer ] = transform apply validator
    def must  ( validator: Validator[ Inner ] ): Validator[ Outer ] = transform apply validator

    /** Provides extended syntax for collections; enables validation rules such as `c.students.each is valid`.
      *
      * @param ev Evidence that the provided expression can be treated as a collection.
      * @tparam Element The element type m of the specified collection.
      * @return Additional syntax (see implementation).
      */
    def each[ Element ]( implicit ev: Inner => Traversable[ Element ] ) =
      new DslContext[ Element, Outer ] {
        private val innerToOuter = DslContext.this.transform
        private val elementToInner = all[ Inner, Element ] _
        protected override def transform = elementToInner andThen innerToOuter
      }

    trait SizeContext {
      def apply( validator: Validator[ Int ] )( implicit ev: Inner => HasSize ): Validator[ Outer ] = {
        val composed = validator.boxed compose { u: Inner => if ( u == null ) null else u.size }
        transform apply composed
      }
    }

    object CollectionDslContext extends /*DslContext[ Inner, Outer ] with*/ SizeContext

    def has = CollectionDslContext
    def have = CollectionDslContext
  }

  trait SimpleDslContext[ U ] extends DslContext[ U, U ] {
    override def transform = identity
  }
}
