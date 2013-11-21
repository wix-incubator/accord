package com.tomergabel.accord.combinators

import com.tomergabel.accord.ResultMatchers
import org.scalatest.{WordSpec, Matchers}

trait CombinatorTestSpec extends WordSpec with Matchers with ResultMatchers {
  protected val testContext = com.tomergabel.accord.stubValidationContext
}
