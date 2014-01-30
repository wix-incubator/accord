/*
  Copyright 2013 Wix.com

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

import scala.reflect.macros.Context
import scala.language.experimental.macros

trait ExpressionDescriber[ C <: Context ] {
  protected val context: C
  import context.universe._

  protected val prototype: ValDef

  private lazy val para = prototype.name

  private object PrototypeSelectorChain {
    def unapplySeq( ouv: Tree ): Option[ Seq[ Name ] ] = ouv match {
      case Select( Ident( `para` ), selector ) => Some( selector :: Nil )
      case Select( PrototypeSelectorChain( elements @ _* ), selector ) => Some( elements :+ selector )
      case _ => None
    }
  }

  /** An extractor for explicitly described validation rules. Applies to validator syntax such as
    * `p.firstName as "described" is notEmpty`, where the `as` parameter (`"described"` in this case) is
    * the extracted description tree.
    */
  private object ExplicitDescriptor {
    private val descriptorTerm = typeOf[ com.wix.accord.dsl.Descriptor[_] ].typeSymbol.name.toTermName
    private val asTerm = newTermName( "as" )

    def unapply( ouv: Tree ): Option[ Tree ] = ouv match {
      case Apply( Select( Apply( TypeApply( Select( _, `descriptorTerm` ), _ ), _ ), `asTerm` ), literal :: Nil ) =>
        Some( literal )
      case _ => None
    }
  }

  protected def renderDescriptionTree( ouv: Tree ): Tree = ouv match {
    case ExplicitDescriptor( description )       => description
    case PrototypeSelectorChain( elements @ _* ) => Literal( Constant( elements.mkString( "." ) ) )
    case Ident( `para` )                         => Literal( Constant( "value" ) )    // Anonymous parameter reference: validator[...] { _ is... }
    case _                                       => Literal( Constant( ouv.toString() ) )
  }
}

private class ExpressionDescriberImpl[ C <: Context, T : C#WeakTypeTag, U : C#WeakTypeTag ]( val context: C, f: C#Expr[ T => U ] )
  extends ExpressionDescriber[ C ] {

  import context.universe._

  val Function( prototype :: prototypeTail, fimpl ) = f.tree
  if ( !prototypeTail.isEmpty )
    // Safety net
    context.abort( prototypeTail.head.pos, "Only single-parameter functions are supported!" )

  def renderedDescription: Expr[ String ] = {
    val desc = renderDescriptionTree( fimpl )
    context.Expr[ String ]( desc )
  }
}

object ExpressionDescriber {
  def apply[ T : c.WeakTypeTag, U : c.WeakTypeTag ]( c: Context )( f: c.Expr[ T => U ] ): c.Expr[ String ] =
    new ExpressionDescriberImpl[ c.type, T, U ]( c, f ).renderedDescription

  def describe[ T, U ]( f: T => U ) = macro ExpressionDescriber[ T, U ]
}