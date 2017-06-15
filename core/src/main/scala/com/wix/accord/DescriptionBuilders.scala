package com.wix.accord

import Descriptions._

trait DescriptionBuilders {

//  implicit class PathOps( base: Path ) {
//    def prepend( element: Description ): Path = element +: base
//    def append( element: Description ): Path = base :+ element
//  }

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