package com.wix.accord

/**
 * Created by tomer on 12/20/14.
 */
trait TestDomain extends Domain {
  type Constraint = AnyRef

  case object OrGroup
  protected def orGroupConstraint = OrGroup
  case object Invalid
  protected def invalidGroupConstraint = Invalid
  case object NotEquals
  protected def notEqualsConstraint[ T ] = _ => NotEquals
  case object Equals
  protected def equalsConstraint[ T ] = _ => Equals
  case object Between
  protected def betweenConstraint[ T ] = _ => Between
  case object GreaterThanEqual
  protected def greaterThanEqualConstraint[ T ] = _ => GreaterThanEqual
  case object EquivalentTo
  protected def equivalentToConstraint[ T ] = _ => EquivalentTo
  case object LesserThanEqual
  protected def lesserThanEqualConstraint[ T ] = _ => LesserThanEqual
  case object GreaterThan
  protected def greaterThanConstraint[ T ] = _ => GreaterThan
  case object LesserThan
  protected def lesserThanConstraint[ T ] = _ => LesserThan
  case object BetweenExclusively
  protected def betweenExclusivelyConstraint[ T ] = _ => BetweenExclusively
  case object StartsWith
  protected def startsWithConstraint = _ => StartsWith
  case object MatchRegex
  protected def matchRegexConstraint = _ => MatchRegex
  case object EndsWith
  protected def endsWithConstraint = _ => EndsWith
  case object FullyMatchRegex
  protected def fullyMatchRegexConstraint = _ => FullyMatchRegex
  case object IsFalse
  protected def isFalseConstraint = IsFalse
  case object IsTrue
  protected def isTrueConstraint = IsTrue
  case object Empty
  protected def emptyConstraint = Empty
  case object NonEmpty
  protected def nonEmptyConstraint = NonEmpty
  case object NullFailure
  protected def nullFailureConstraint = NullFailure
  case object NullFailureNeg
  protected def nullFailureConstraintNeg = NullFailureNeg
}
