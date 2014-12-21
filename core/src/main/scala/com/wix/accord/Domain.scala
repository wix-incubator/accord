package com.wix.accord

import com.wix.accord.combinators.Combinators
import com.wix.accord.dsl.OrderingOps

trait Domain
  extends Validation
  with Constraints
  with Results
  with Combinators
  with dsl.DSL {

  implicit val domain: Domain = this
  protected def newOrderingOps( snippet: String ): OrderingOps    // Haven't found a way around it yet.
}
