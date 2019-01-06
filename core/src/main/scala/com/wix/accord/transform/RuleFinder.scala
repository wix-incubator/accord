/*
  Copyright 2013-2019 Wix.com

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
import com.wix.accord.Descriptions.{Description, Explicit, Path}
import com.wix.accord._

private[ transform ] trait RuleFinder[ C <: Context ] extends PatternHelper[ C ] with MacroHelper[ C ] {
  self: MacroLogging[ C ] with ExpressionDescriber[ C ] =>

  import context.universe._
  import context.abort

  protected def prototype: ValDef

  sealed trait ValidatorApplication
  protected case class BooleanExpression( expr: Tree ) extends ValidatorApplication
  protected case class ValidationRule( ouv: Tree, validation: Tree, path: Expr[ Path ] )
    extends ValidatorApplication

  protected object ValidationRule {
    def isValidationRule( tpe: Type ): Boolean = !tpe.isBottom && tpe <:< typeOf[ Validator[_] ]
    def isValidationRule( tree: Tree ): Boolean = isValidationRule( tree.tpe )
    def unapply( t: Tree ): Option[ Tree ] = if ( isValidationRule( t ) ) Some( t ) else None
  }

  /** An extractor for validation rules. The object under validation is, by design, wrapped in the implicit
    * DSL construct [[com.wix.accord.dsl.Contextualizer]], so that a validation rule can be defined with
    * syntax like `p.firstName is notEmpty`.
    *
    * In the example above, `p.firstName` is the expression wrapped by [[com.wix.accord.dsl.Contextualizer]]
    * and yields the Object Under Validation (OUV).
    */
  object ValidatorApplication {
    private val contextualizerTerm = typeOf[ dsl.Contextualizer[_] ].typeSymbol.name.toTermName

    private def extractObjectUnderValidation( t: Tree ): List[ Tree ] =
      collectFromPattern( t ) {
        case Apply( TypeApply( Select( _, `contextualizerTerm` ), _ :: Nil ), e :: Nil ) =>
          resetAttrs( e.duplicate )
      }

    private object AtLeastOneSelect {
      private def unapplyInternal( tree: Tree ): Option[ Tree ] = tree match {
        case Select( from, _ ) => unapplyInternal( from )
        case terminal => Some( terminal )
      }

      def unapply( tree: Tree ): Option[ Tree ] = tree match {
        case Select( from, _ ) => unapplyInternal( from )
        case terminal => None
      }
    }

    private def rewriteContextExpressionAsValidator( expr: Tree ) =
      transformByPattern( expr ) {
        case root @ Apply( AtLeastOneSelect( Apply( TypeApply( Select( _, `contextualizerTerm` ), _ ), _ :: Nil ) ), _ :: Nil ) =>
          rewriteExistentialTypes( root )
      }

    object ObjectUnderValidation {
      def unapply( t: Tree ): Option[ List[ Tree ] ] =
        ValidationRule.unapply( t ) map extractObjectUnderValidation
    }

    def unapply( expr: Tree ): Option[ ValidatorApplication ] = expr match {
      case ObjectUnderValidation( Nil ) =>
        abort( expr.pos, s"Failed to extract object under validation from tree $expr (type=${expr.tpe}, raw=${showRaw(expr)})" )

      case ObjectUnderValidation( ouv :: Nil ) =>
        val sv = rewriteContextExpressionAsValidator( expr )
        val path =
          ExplicitlyDescribed.unapply( expr )
            .map { explicit => q"com.wix.accord.Descriptions.Path( $explicit )" }
            .getOrElse( describeTree( prototype, ouv ) )
        trace( s"""
              |Found validation rule:
              |  ouv=$ouv
              |  ouvraw=${showRaw(ouv)}
              |  sv=${show(sv)}
              |  svraw=${showRaw(sv)}
              |  path=$path
              |""".stripMargin, ouv.pos )
        Some( ValidationRule( ouv, sv, context.Expr[ Path ]( path ) ) )

      case ObjectUnderValidation( _ :: _ ) =>
        // Multiple validators found; this can happen in case of a multiple-clause boolean expression,
        // e.g. "(f1 is notEmpty) or (f2 is notEmpty)".
        Some( BooleanExpression( expr ) )

      case Block( ValidatorApplication( va ) :: Nil, q"()" ) =>
        // In some scenarios (e.g. a code block with a validator application as its last statement) the Scala
        // compiler wraps it with a Unit-typed anonymous block.
        Some( va )

      case _ => None
    }

    def isValid( expr: Tree ): Boolean = unapply( expr ).isDefined
  }

  object PartialFunction {
    def unapply( tree: Tree ): Option[ Seq[ CaseDef ] ] = {
      var l = scala.collection.mutable.ArrayBuffer.empty[ CaseDef ]

      val traverser = new Traverser {
        override def traverseCases( cases: List[ CaseDef ] ): Unit =
          if ( currentOwner.isMethod && currentOwner.asMethod.name.decodedName.toString == "applyOrElse" ) {
            l ++= cases
          } else super.traverseCases( cases )
      }.traverse( tree )

      if ( l.isEmpty ) None else Some( l.toSeq )
    }
  }
}
