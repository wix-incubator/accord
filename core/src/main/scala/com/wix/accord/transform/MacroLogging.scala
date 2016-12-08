/*
  Copyright 2013-2016 Wix.com

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

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

