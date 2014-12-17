package com.wix.accord.simple

import com.wix.accord.{Constraints, Domain}
import com.wix.accord.combinators._

/**
 * Created by tomer on 12/16/14.
 */

sealed trait SimpleConstraints
  extends Constraints
  with BooleanCombinatorConstraints
  with GeneralPurposeCombinatorConstraints
  with CollectionCombinatorConstraints
  with OrderingCombinatorConstraints
  with StringCombinatorConstraints
{
  type Constraint = String
  protected def nullFailureConstraint = "is a null"
  protected def nullFailureConstraintNeg = "is not a null"

  protected def isFalseConstraint = "must be true"
  protected def isTrueConstraint = "must be false"

  protected def orGroupConstraint = "doesn't meet any of the requirements"
  protected def invalidGroupConstraint = "is invalid"
  protected def equalsConstraint[ T ] = to => s"does not equal $to"
  protected def notEqualsConstraint[ T ] = to => s"equals $to"

  protected def emptyConstraint = "must not be empty"
  protected def nonEmptyConstraint = "must be empty"

  protected def betweenConstraint[ T ] = {
    case ( prefix, value, lowerBound, upperBound ) =>
      s"$prefix $value, expected between $lowerBound and $upperBound"
  }
  protected def greaterThanEqualConstraint[ T ] = {
    case Bound( prefix, value, bound ) => s"$prefix $value, expected $bound or more"
  }
  protected def equivalentToConstraint[ T ] = {
    case Bound( prefix, value, other ) => s"$prefix $value, expected $other"
  }
  protected def greaterThanConstraint[ T ] = {
    case Bound( prefix, value, bound ) => s"$prefix $value, expected more than $bound"
  }
  protected def lesserThanConstraint[ T ] = {
    case Bound( prefix, value, bound ) => s"$prefix $value, expected less than $bound"
  }
  protected def lesserThanEqualConstraint[ T ] = {
    case Bound( prefix, value, bound ) => s"$prefix $value, expected $bound or less"
  }
  protected def betweenExclusivelyConstraint[ T ] = {
    case ( prefix, value, lowerBound, upperBound ) =>
      s"$prefix $value, expected between $lowerBound and $upperBound (exclusively)"
  }

  protected def startsWithConstraint = prefix => s"must start with '$prefix'"
  protected def endsWithConstraint = suffix => s"must end with '$suffix'"
  protected def matchRegexConstraint = pattern => s"must match regular expression '$pattern'"
  protected def fullyMatchRegexConstraint = pattern => s"must fully match regular expression '$pattern'"
}

trait SimpleDomain extends Domain with SimpleConstraints

package object simple extends SimpleDomain