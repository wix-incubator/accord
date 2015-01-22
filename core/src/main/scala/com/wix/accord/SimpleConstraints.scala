package com.wix.accord

import java.util.regex.Pattern

import com.wix.accord.combinators._

/**
 * Created by tomer on 12/16/14.
 */

private[ accord ] trait SimpleConstraints
  extends Constraints
  with BooleanCombinatorConstraints
  with GeneralPurposeCombinatorConstraints
  with CollectionCombinatorConstraints
  with OrderingCombinatorConstraints
  with StringCombinatorConstraints {

  type Constraint = String

  protected def isNullConstraint = "is a null"
  protected def isNotNullConstraint = "is not a null"

  protected def isFalseConstraint = "must be true"
  protected def isTrueConstraint = "must be false"

  protected def noMatchingClauseConstraint = "doesn't meet any of the requirements"
  protected def invalidGroupConstraint = "is invalid"
  protected def equalToConstraint[ T ]( to: T ) = s"does not equal $to"
  protected def notEqualToConstraint[ T ]( to: T ) = s"equals $to"

  protected def emptyConstraint = "must be empty"
  protected def notEmptyConstraint = "must not be empty"

  protected def betweenConstraint[ T ]( prefix: String, value: T, lower: T, upper: T ) =
    s"$prefix $value, expected between $lower and $upper"
  protected def betweenExclusivelyConstraint[ T ]( prefix: String, value: T, lower: T, upper: T ) =
    s"$prefix $value, expected between $lower and $upper (exclusively)"

  protected def greaterThanConstraint[ T ]( prefix: String, value: T, bound: T ) =
    s"$prefix $value, expected more than $bound"
  protected def greaterThanEqualConstraint[ T ]( prefix: String, value: T, bound: T ) =
    s"$prefix $value, expected $bound or more"
  protected def lesserThanConstraint[ T ]( prefix: String, value: T, bound: T ) =
    s"$prefix $value, expected less than $bound"
  protected def lesserThanEqualConstraint[ T ]( prefix: String, value: T, bound: T ) =
    s"$prefix $value, expected $bound or less"
  protected def equivalentToConstraint[ T ]( prefix: String, value: T, other: T ) =
    s"$prefix $value, expected $other"

  protected def startsWithConstraint( prefix: String ) = s"must start with '$prefix'"
  protected def matchRegexConstraint( pattern: Pattern ) = s"must match regular expression '$pattern'"
  protected def endsWithConstraint( suffix: String ) = s"must end with '$suffix'"
  protected def fullyMatchRegexConstraint( pattern: Pattern ) = s"must fully match regular expression '$pattern'"
}
