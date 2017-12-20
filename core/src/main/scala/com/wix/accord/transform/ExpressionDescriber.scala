/*
  Copyright 2013-2017 Wix.com

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
import com.wix.accord.Descriptions._

import scala.language.experimental.macros

/** A macro helper trait that generates implicit description for expressions. The transformation operates in the
  * context of a function of the form `Function1[ T, U ]`, or in other words only supports single-parameter
  * functions.
  *
  * A given expression can be transformed with [[com.wix.accord.transform.ExpressionDescriber.describeTree]]
  * based on the following rules:
  *  - Selectors over the function prototype are rewritten to [[com.wix.accord.Descriptions.AccessChain AccessChains]];
  *    for example, `{ p: Person => p.firstName }` gets rewritten to a tree representing
  *    `AccessChain( Generic( "firstName" ) )`
  *  - Explicitly described expressions (via [[com.wix.accord.dsl.Descriptor]]) are rewritten to
  *    [[com.wix.accord.Descriptions.Explicit Explicit]] descriptions, for example
  *    `{ p: Person => p.firstName as "first name" }` gets rewritten to a tree representing `Explicit( "first name" )`
  *  - An anonymous parameter reference gets rewritten to [[com.wix.accord.Descriptions.SelfReference SelfReference]];
  *    for example in `validator[ String ] { _ is notEmpty }` gets rewritten to a tree representing `SelfReference`.
  *  - Any other expression is rewritten as a [[com.wix.accord.Descriptions.Generic Generic]]
  *    description, for example `{ _ => 1 + 2 + 3 }` gets rewritten as `Generic( "1 + 2 + 3" )`.
  *
  * @tparam C The macro context type
  */
private[ transform ] trait ExpressionDescriber[ C <: Context ] extends MacroHelper[ C ] with PatternHelper[ C ] {
  import context.universe._

  private val tokenLookup = "(\\s+[\\)\\}]?)+".r

  protected def prettyPrint( tree: Tree ): String = {
    val fileContent = new String( tree.pos.source.content )
    val start = tree.collect { case t if !t.isEmpty => startPos( t.pos ) }.min
    val end = Math.max( start, tree.collect { case t if !t.isEmpty => endPos( t.pos ) }.max ) + 1
    val codeSlice =
      tokenLookup.findFirstMatchIn( fileContent.substring( end ) ) match {
        case None =>
          fileContent.substring( start )

        case Some( tokenEndOffset ) =>
          // For whatever reason, in some cases the compiler provides a tree position that ends after the first
          // character of the last token. We'll therefore look up the next token and artificially add the missing
          // characters to the code slice prior to parsing, taking parentheses into account.
          fileContent.substring( start, end + tokenEndOffset.end )
      }

    val parser = newUnitParser( codeSlice )
    try parser.expr()
    catch {
      case e: Exception =>
        context.abort( tree.pos, s"""Failed to pretty print expression "$tree", code slice: "$codeSlice", message: ${e.getMessage}""" )
    }
    fileContent.slice( start, start + parser.in.lastOffset )
  }

  /** An extractor for explicitly described expressions. Applies expressions like
    * `p.firstName as "described"`, where the `as` parameter (`"described"` in this case) is the extracted
    * description tree.
    */
  case object ExplicitlyDescribed {
    def unapply( ouv: Tree ): Option[ Tree ] =
      collectFromPattern( ouv ) {
        case q"com.wix.accord.dsl.`package`.Descriptor[$_]( $_ ).as( $description )" =>
          q"com.wix.accord.Descriptions.Explicit( ${ resetAttrs( description.duplicate ) } )"
      }.headOption
  }

  /** Generates a description for the specified AST.
    *
    * @param prototype The function prototype; specifically, the single function parameter's definition as
    *                  a `ValDef`. Must be provided by the inheritor.
    * @return The generated description.
    */
  protected def describeTree( prototype: ValDef, ouv: Tree ): Tree = {
    val PrototypeName = prototype.name

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
    object PrototypeSelectorChain {
      def unapplySeq( ouv: Tree ): Option[ Seq[ Name ] ] = ouv match {
        case Select( Ident( PrototypeName ), selector ) => Some( selector :: Nil )
        case Select( PrototypeSelectorChain( elements @ _* ), selector ) => Some( elements :+ selector )
        case _ => None
      }
    }

    val pathElements: Seq[ Tree ] =
      ouv match {
        case ExplicitlyDescribed( description ) =>
          description :: Nil

        case PrototypeSelectorChain( elements @ _* ) =>
          def renderName( n: Name ) = q"com.wix.accord.Descriptions.Generic( ${ n.decodedName.toString } )"
          elements map renderName

        case Ident( PrototypeName ) =>
          // Anonymous parameter reference: validator[...] { _ is... }
//          q"com.wix.accord.Descriptions.SelfReference" :: Nil
          Nil

        case _ =>
          genericDescription( ouv ) :: Nil
      }

    q"com.wix.accord.Descriptions.Path( ..$pathElements )"
  }

  protected def genericDescription( tree: Tree ): Tree =
    q"com.wix.accord.Descriptions.Generic( ${ prettyPrint( tree ) } )"
}

/** A helper class which builds on [[com.wix.accord.transform.ExpressionDescriber]] to describe function literals. */
private[ transform ] trait FunctionDescriber[ C <: Context, T, U ]
  extends ExpressionDescriber[ C ]
{
  import context.universe._

  def describeFunction( f: Expr[ T => U ] ): ( ValDef, Tree ) =
    f.tree match {
      case Function( proto :: Nil, impl ) =>
        ( proto, impl )

      case Function( _ :: tail, _ ) if tail != Nil =>
        context.abort( tail.head.pos, "Only single-parameter functions are supported!" )

      case _ =>
        context.abort( f.tree.pos,
          """
            |Only function literals are supported; function parameters (val f: T => U = ...) cannot be resolved at
            |compile time.
          """.stripMargin )
    }
}

private class TestFunctionDescriber[ C <: Context, T, U ]( val context: C, f: C#Expr[ T => U ] )
  extends FunctionDescriber[ C, T, U ]
{
  import context.universe._

  val ( prototype, implementation ) = describeFunction( f in context.mirror )

  /** Renders a description for the function body and externalizes it as a string expression. */
  def renderedPath: Expr[ Path ] = context.Expr[ Path ]( describeTree( prototype, implementation ) )
}

private[ accord ] object ExpressionDescriber {

  def apply[ T : c.WeakTypeTag, U : c.WeakTypeTag ]( c: Context )( f: c.Expr[ T => U ] ): c.Expr[ Path ] =
    new TestFunctionDescriber[ c.type, T, U ]( c, f ).renderedPath

  /** A test invoker for [[com.wix.accord.transform.ExpressionDescriber]] */
  def describe[ T, U ]( f: T => U ): Path = macro ExpressionDescriber[ T, U ]
}
