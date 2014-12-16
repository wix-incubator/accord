package com.wix.accord

import com.wix.accord.combinators.{CombinatorConstraints, Combinators}

trait Domain
  extends Validation
  with Constraints
  with Results
  with Combinators

