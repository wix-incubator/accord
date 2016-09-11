package com.wix.accord

import com.wix.accord.Descriptions.Generic
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by grenville on 9/6/16.
  */
class ViolationSpec extends FlatSpec with Matchers {

  val subSampleRule = RuleViolation("value", "is null", Generic("subSampleRule"))
  val subSampleGroup = GroupViolation("subgroup", "is not valid",
    children = Set(subSampleRule), Generic("request"))
  val sampleRule1 = RuleViolation("value", "is null", Generic("sampleRule1"))
  val sampleRule2 = RuleViolation("value", "is null", Generic("sampleRule2"))
  val sampleGroup = GroupViolation("group", "is not valid",
    children = Set(sampleRule1, sampleRule2, subSampleGroup), Generic("request"))

  "Violation" should "produce a string representation for a RuleViolation" in {
    sampleRule1.toString shouldEqual "sampleRule1 is null"
  }

  it should "produce a string representation for a GroupViolation" in {
    sampleGroup.toString shouldEqual "request is not valid: Set(sampleRule1 is null, sampleRule2 is null, " +
      "subgroup is not valid: Set(subSampleRule is null))"
  }
}
