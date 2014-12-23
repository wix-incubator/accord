package com.wix.accord

import java.util.regex.Pattern

import com.wix.accord.scalatest.ResultMatchers

/**
 * Created by tomer on 12/20/14.
 */
trait TestDomain extends Domain {
  trait Constraint

  object Constraints {
    import java.util.regex.Pattern

    case object NoMatch extends Constraint
    case object Invalid extends Constraint
    case class NotEqualTo[ T ]( to: T ) extends Constraint
    case class EqualTo[ T ]( to: T ) extends Constraint
    case class Between[ T ]( lower: T, upper: T ) extends Constraint
    case class GreaterThanEqual[ T ]( bound: T ) extends Constraint
    case class EquivalentTo[ T ]( bound: T ) extends Constraint
    case class LesserThanEqual[ T ] ( bound: T ) extends Constraint
    case class GreaterThan[ T ] ( bound: T ) extends Constraint
    case class LesserThan[ T ] ( bound: T ) extends Constraint
    case class BetweenExclusively[ T ]( lower: T, upper: T ) extends Constraint
    case class StartsWith( prefix: String ) extends Constraint
    case class EndsWith( suffix: String ) extends Constraint
    case class MatchRegex( pattern: Pattern ) extends Constraint
    case class FullyMatchRegex( pattern: Pattern ) extends Constraint
    case object IsFalse extends Constraint
    case object IsTrue extends Constraint
    case object Empty extends Constraint
    case object NonEmpty extends Constraint
    case object IsNull extends Constraint
    case object IsNotNull extends Constraint
  }

  import Constraints._
  protected def noMatchingClauseConstraint = NoMatch
  protected def invalidGroupConstraint = Invalid
  protected def startsWithConstraint( prefix: String ) = StartsWith( prefix )
  protected def matchRegexConstraint( pattern: Pattern ) = MatchRegex( pattern )
  protected def endsWithConstraint( suffix: String ) = EndsWith( suffix )
  protected def fullyMatchRegexConstraint( pattern: Pattern ) = FullyMatchRegex( pattern )
  protected def betweenConstraint[ T ]( prefix: String, value: T, lower: T, upper: T ) = Between( lower, upper )
  protected def greaterThanEqualConstraint[ T ]( prefix: String, value: T, bound: T ) = GreaterThanEqual( bound )
  protected def equivalentToConstraint[ T ]( prefix: String, value: T, other: T ) = EquivalentTo( other )
  protected def lesserThanEqualConstraint[ T ]( prefix: String, value: T, bound: T ) = LesserThanEqual( bound )
  protected def greaterThanConstraint[ T ]( prefix: String, value: T, bound: T ) = GreaterThan( bound )
  protected def lesserThanConstraint[ T ]( prefix: String, value: T, bound: T ) = LesserThan( bound )
  protected def betweenExclusivelyConstraint[ T ]( prefix: String, value: T, lower: T, upper: T ) =
    BetweenExclusively( lower, upper )
  protected def notEqualToConstraint[ T ]( to: T ) = NotEqualTo( to )
  protected def equalToConstraint[ T ]( to: T ) = EqualTo( to )
  protected def isFalseConstraint = IsFalse
  protected def isTrueConstraint = IsTrue
  protected def emptyConstraint = Empty
  protected def nonEmptyConstraint = NonEmpty
  protected def isNullConstraint = IsNull
  protected def isNotNullConstraint = IsNotNull
}

import org.scalatest.Suite
trait TestDomainMatchers extends ResultMatchers[ TestDomain ] {
  self: Suite =>
}

object TestDomain extends TestDomain
