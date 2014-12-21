package com.wix.accord

/**
 * Created by tomer on 12/16/14.
 */
trait Constraints {
  type Constraint
  protected def isNullConstraint: Constraint
  protected def notNullConstraint: Constraint
}
