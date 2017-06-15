package com.wix.accord

trait ResultBuilders {

  implicit class ViolationValueOps( base: Violation ) {
    def withValue( v: Any ): Violation = base match {
      case rv: RuleViolation => rv.copy( value = v )
      case gv: GroupViolation => gv.copy( value = v )
    }
  }

  implicit class ResultValueOps( base: Result ) {
    def withValue( v: Any ): Result =
      base map { violations => violations map { _ withValue v } }
  }
}

object ResultBuilders extends ResultBuilders