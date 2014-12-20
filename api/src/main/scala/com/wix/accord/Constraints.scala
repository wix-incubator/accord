package com.wix.accord

/**
 * Created by tomer on 12/16/14.
 */
trait Constraints {
  type Constraint
  protected def nullFailureConstraint: Constraint
  protected def nullFailureConstraintNeg: Constraint
}

trait ConstraintBuilders {
  self: Constraints =>

  protected type ConstraintBuilder[ -T ] = T => Constraint

  import scala.language.implicitConversions

  implicit def elevateStaticConstraintToBuilder( constraint: Constraint ): ConstraintBuilder[ Any ] =
    _ => constraint
}

