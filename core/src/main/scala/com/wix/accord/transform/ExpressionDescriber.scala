/*
  Copyright 2013-2015 Wix.com

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

import MacroHelper._
import scala.language.experimental.macros

/** A macro helper trait that generates implicit description for expressions. The transformation operates in the
  * context of a function of the form `Function1[ T, U ]`, or in other words only supports single-parameter
  * functions.
  *
  * The expression is transformable via [[com.wix.accord.transform.ExpressionDescriber.describeTree]]
  * based on the following rules:
  *  - Selectors over the function prototype are rewritten to the selected expression; for example,
  *    `{ p: Person => p.firstName }` gets rewritten to a tree representing the string literal `"firstName"`
  *  - Explicitly described expressions (via [[com.wix.accord.dsl.Descriptor]]) are rewritten to a tree
  *    representing the description as a string literal, for example `{ p: Person => p.firstName as "first name" }`
  *    gets rewritten simply as `"first name"`
  *  - Any other expression is rewritten as tree representing a string literal of the expression itself, for
  *    example `{ _ => 1 + 2 + 3 }` gets rewritten as `"1 + 2 + 3"`.
  *
  * @tparam C The macro context type
  */
private[ accord ] trait ExpressionDescriber[ C <: Context ] extends MacroHelper[ C ] with PatternHelper[ C ] {
  import context.universe._

  /** The function prototype; specifically, the single function parameter's definition as a `ValDef`. Must be
    * provided by the inheritor.
    */
  protected val prototype: ValDef

  sealed trait Description
  case class ExplicitDescription( tree: Tree ) extends Description
  case class GenericDescription( tree: Tree ) extends Description
  case class AccessChain( elements: Seq[ Name ] ) extends Description
  case object SelfReference extends Description

  private lazy val para = prototype.name

  /** A helper extractor object that handles selector chains recursively. The innermost selector must select
    * over the function prototype.
    *
    * For example, for the function `{ p: Person => p.firstName.size }` and the input representing the tree of the
    * function definition, the AST will look like this:
    *
    *  `Select(Select(Ident(newTermName("p")), newTermName("firstName")), newTermName("size"))`
    *
    * This in turn gets extracted as `PrototypeSelectorChain( "firstName" :: "size" :: Nil )`.
    */
  private object PrototypeSelectorChain {
    def unapplySeq( ouv: Tree ): Option[ Seq[ Name ] ] = ouv match {
      case Select( Ident( `para` ), selector ) => Some( selector :: Nil )
      case Select( PrototypeSelectorChain( elements @ _* ), selector ) => Some( elements :+ selector )
      case _ => None
    }
  }

  /** An extractor for explicitly described expressions. Applies expressions like
    * `p.firstName as "described"`, where the `as` parameter (`"described"` in this case) is the extracted
    * description tree.
    */
  case object ExplicitDescription {
    private val descriptorTerm = typeOf[ com.wix.accord.dsl.Descriptor[_] ].typeSymbol.name.toTermName
    private val asTerm = termName( "as" )

    def unapply( description: Description ): Option[ Tree ] = description match {
      case ed: ExplicitDescription => Some( ed.tree )
      case _ => None
    }

    private[ ExpressionDescriber ] def unapply( ouv: Tree ): Option[ ExplicitDescription ] = ouv match {
      case Apply( Select( Apply( TypeApply( Select( _, `descriptorTerm` ), _ ), _ ), `asTerm` ), literal :: Nil ) =>
        Some( ExplicitDescription( literal ) )
      case _ => None
    }
  }

  /** Generates a description for the specified AST.
    *
    * @param ouv The AST representing the function body part for which a description is to be rendered.
    * @return The description, represented as a string literal.
    */
  protected def describeTree( ouv: Tree ): Description = ouv match {
    case ExplicitDescription( description )      => description
    case PrototypeSelectorChain( elements @ _* ) => AccessChain( elements )
    case Ident( `para` )                         => SelfReference    // Anonymous parameter reference: validator[...] { _ is... }
    case _                                       => GenericDescription( ouv )
  }
}

/** A helper class which builds on [[com.wix.accord.transform.ExpressionDescriber]] to describe function literals. */
private class FunctionDescriber[ C <: Context, T : C#WeakTypeTag, U : C#WeakTypeTag ]( val context: C, f: C#Expr[ T => U ] )
  extends ExpressionDescriber[ C ] {

  import context.universe._

  val ( prototype, fimpl ) = f.tree match {
    case Function( proto :: Nil, impl ) => ( proto, impl )
    case Function( _ :: tail, _ ) if tail != Nil =>
      context.abort( tail.head.pos, "Only single-parameter functions are supported!" )
    case _ =>
      context.abort( f.tree.asInstanceOf[ context.Tree ].pos,
        """
          |Only function literals are supported; function parameters (val f: T => U = ...) cannot be resolved at
          |compile time.
        """.stripMargin )
  }

  /** Renders a description for the function body and externalizes it as a string expression. */
  def renderedDescription: Expr[ String ] = {
    val desc = describeTree( fimpl )
    context.Expr[ String ]( Literal( Constant( showRaw( desc ) ) ) )
  }
}

private[ accord ] object ExpressionDescriber {

  def apply[ T : c.WeakTypeTag, U : c.WeakTypeTag ]( c: Context )( f: c.Expr[ T => U ] ): c.Expr[ String ] =
    new FunctionDescriber[ c.type, T, U ]( c, f ).renderedDescription

  /** A test invoker for [[com.wix.accord.transform.ExpressionDescriber]] */
  def describe[ T, U ]( f: T => U ): String = macro ExpressionDescriber[ T, U ]
}
