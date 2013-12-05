package com.tomergabel.accord.specs2

import org.specs2.mutable.Specification
import com.tomergabel.accord.{Violation, GroupViolation, RuleViolation}
import org.specs2.matcher.{MatchResult, Matcher}

class ResultMatchersSpec extends Specification with ResultMatchers {

  "RuleViolationMatcher" should {

    val sampleViolation = RuleViolation( "value", "constraint", "description" )

    "correctly match a rule violation based on value" in {
      val matchRule = RuleViolationMatcher( value = "value" )
      sampleViolation should matchRule
    }

    "correctly match a rule violation based on constraint" in {
      val matchRule = RuleViolationMatcher( constraint = "constraint" )
      sampleViolation should matchRule
    }

    "correctly match a rule violation based on description" in {
      val matchRule = RuleViolationMatcher( description = "description" )
      sampleViolation should matchRule
    }

    "fail to match a non-matching rule violation" in {
      val matchRule = RuleViolationMatcher( value = "incorrect" )
      sampleViolation should not( matchRule )
    }
  }

  "GroupViolationMatcher" should {

    val sampleRule = RuleViolation( "value", "constraint", "description" )
    val sampleGroup = GroupViolation( value = "group", constraint = "violation", description = "ftw", children = Seq( sampleRule ) )

    "correctly match a group violation based on value" in {
      val matchRule = GroupViolationMatcher( value = "group" )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on constraint" in {
      val matchRule = GroupViolationMatcher( constraint = "violation" )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on description" in {
      val matchRule = GroupViolationMatcher( description = "ftw" )
      sampleGroup should matchRule
    }

    "correctly match a rule violation based on children" in {
      val matchChildRule = RuleViolationMatcher( value = "value", constraint = "constraint", description = "description" )
      val matchRule = GroupViolationMatcher( violations = Seq( matchChildRule ) )
      sampleGroup should matchRule
    }

    "fail to match a non-matching group violation due to a constraint on a group property" in {
      val matchRule = GroupViolationMatcher( value = "incorrect" )
      sampleGroup should not( matchRule )
    }

    "fail to match a non-matching group violation due to a nonmatching child rule" in {
      val nonmatchingChildRule = RuleViolationMatcher( value = "incorrect" )
      val matchRule = GroupViolationMatcher( violations = Seq( nonmatchingChildRule ) )
      sampleGroup should not( matchRule )
    }
  }

  "Matcher construction DSL" should {

    "generate a correct rule violation for a Tuple2[String, String]" in {
      val rv: RuleViolationMatcher = "description" -> "constraint"
      rv.description shouldEqual "description"
      rv.constraint shouldEqual "constraint"
      rv.value should beNull
    }

    "generate a correct group violation via group()" in {
      val gv = group( "description", "constraint", "description" -> "constraint" )
      gv.description shouldEqual "description"
      gv.constraint shouldEqual "constraint"
      gv.value should beNull
      gv.violations should have size 1
      gv.violations.head should beLike {
        case rv: RuleViolationMatcher =>
          ( rv.description shouldEqual "description" ) and
          ( rv.constraint shouldEqual "constraint" ) and
          ( rv.value should beNull )
      }
    }
  }

}
