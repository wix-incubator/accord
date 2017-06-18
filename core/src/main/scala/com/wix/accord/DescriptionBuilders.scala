package com.wix.accord

import Descriptions._

trait DescriptionBuilders extends LowPriorityDescriptionBuilders {

  implicit class FailurePathOps( base: Failure ) {
    def withPath( f: Path => Path ): Failure =
      base map { violations => violations map { _ withPath f } }

    def prepend( desc: Description ): Failure =
      withPath( desc +: _ )

    def prepend( path: Path ): Failure =
      withPath( path ++ _ )

    def append( desc: Description ): Failure =
      withPath( _ :+ desc )

    def append( path: Path ): Failure =
      withPath( _ ++ path )
  }
}

trait LowPriorityDescriptionBuilders {

  implicit class ViolationPathOps( base: Violation ) {
    def withPath( f: Path => Path ): Violation = base match {
      case rv: RuleViolation => rv.copy( path = f( base.path ) )
      case gv: GroupViolation => gv.copy( path = f( base.path ) )
    }
  }

  implicit class ResultPathOps( base: Result ) {
    def withPath( f: Path => Path ): Result =
      base map { violations => violations map { _ withPath f } }

    def prepend( desc: Description ): Result =
      withPath( desc +: _ )

    def prepend( path: Path ): Result =
      withPath( path ++ _ )

    def append( desc: Description ): Result =
      withPath( _ :+ desc )

    def append( path: Path ): Result =
      withPath( _ ++ path )
  }
}

object DescriptionBuilders extends DescriptionBuilders