package com.wix.accord.transform

import com.wix.accord.transform.MacroHelper._

/**
  * Created by tomerga on 07/03/2016.
  */
private[ transform ] trait MacroLogging[ C <: Context ] {
  /** The macro context; inheritors must provide this */
  protected val context: C

  import context.universe._
  import context.info

  protected def debugOutputEnabled: Boolean
  protected def traceOutputEnabled: Boolean

  def debug( s: => String, pos: Position = context.enclosingPosition ) =
    if ( debugOutputEnabled ) info( pos, s, force = false )
  def trace( s: => String, pos: Position = context.enclosingPosition ) =
    if ( traceOutputEnabled ) info( pos, s, force = false )
}

