package com.wix.accord

import com.wix.accord.combinators.Combinators

trait Domain
  extends Validation
  with Constraints
  with Results
  with Combinators
  with dsl.DSL {
}