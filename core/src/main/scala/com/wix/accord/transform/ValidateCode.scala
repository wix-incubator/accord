/*
 * Copyright (c) 2013 Miles Sabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wix.accord.transform

import scala.language.experimental.macros
import scala.reflect.macros.Context

/**
  * A utility which ensures that a code fragment does not typecheck.
  *
  * Credit: Stefan Zeiger (@StefanZeiger)
  *
  * Adapted from the Shapeless library by Miles Sabin
  * (https://github.com/milessabin/shapeless/blob/master/core/src/main/scala/shapeless/test/typechecking.scala)
  */
private[ accord ] object ValidateCode {
  def apply( code: String ): Option[ String ] = macro applyImpl

  def applyImpl( c: Context )( code: c.Expr[ String ] ): c.Expr[ Option[ String ] ] = {
    import c.universe._

    val codeStr = c.eval( c.Expr[ String ]( c.resetLocalAttrs( code.tree.duplicate ) ) )
    try {
      c.typeCheck( c.parse( "{ " + codeStr + " }" ) )
      reify( None )
    } catch {
      case e: Exception =>
        val rendered = {
          val sw = new java.io.StringWriter()
          val pw = new java.io.PrintWriter( sw )
          e printStackTrace pw
          pw.flush()
          sw.toString
        }
        c.Expr[ Option[ String ] ]( q"Some( $rendered )" )
    }
  }
}