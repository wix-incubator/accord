/*
  Copyright 2013-2014 Wix.com

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
import com.wix.accord._
import com.wix.accord.transform.ValidationTransform.TransformedValidator

private[ transform ] trait MacroLogging[ C <: Context ] {
  /** The macro context; inheritors must provide this */
  protected val context: C

  import context.universe._
  import context.info

  protected def debugOutputEnabled: Boolean
  protected def traceOutputEnabled: Boolean

  def debug( s: String, pos: Position = context.enclosingPosition ) =
    if ( debugOutputEnabled ) info( pos, s, force = false )
  def trace( s: String, pos: Position = context.enclosingPosition ) =
    if ( traceOutputEnabled ) info( pos, s, force = false )
}

private[ transform ] trait ExpressionFinder[ C <: Context ] extends PatternHelper[ C ] with ExpressionDescriber[ C ] {
  self: MacroLogging[ C ] =>

  import context.universe._
  import context.abort

  sealed trait ValidatorApplication
  protected case class BooleanExpression( expr: Tree ) extends ValidatorApplication
  protected case class Subvalidator( description: Tree, ouv: Tree, validation: Tree ) extends ValidatorApplication

  /** An extractor for validation rules. The object under validation is, by design, wrapped in the implicit
    * DSL construct [[com.wix.accord.dsl.Contextualizer]], so that a validation rule can be defined with
    * syntax like `p.firstName is notEmpty`.
    *
    * In the example above, `p.firstName` is the expression wrapped by [[com.wix.accord.dsl.Contextualizer]]
    * and yields the Object Under Validation (OUV).
    */
  object ValidatorApplication {
    private val contextualizerTerm = typeOf[ dsl.Contextualizer[_] ].typeSymbol.name.toTermName
    private val validatorType = typeOf[ Validator[_] ]

    private def extractObjectUnderValidation( t: Tree ): List[ Tree ] =
      collectFromPattern( t ) {
        case Apply( TypeApply( Select( _, `contextualizerTerm` ), tpe :: Nil ), e :: Nil ) =>
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

    def unapply( expr: Tree ): Option[ ValidatorApplication ] = expr match {
      case t if t.tpe <:< validatorType =>
        extractObjectUnderValidation( expr ) match {
          case Nil =>
            abort( t.pos, s"Failed to extract object under validation from tree $t (raw=${showRaw(t)})" )

          case ouv :: Nil =>
            val sv = rewriteContextExpressionAsValidator( expr )
            val desc = renderDescriptionTree( ouv )
            trace( s"""
                  |Found subvalidator:
                  |  ouv=$ouv
                  |  ouvraw=${showRaw(ouv)}
                  |  sv=${show(sv)}
                  |  svraw=${showRaw(sv)}
                  |  desc=$desc
                  |""".stripMargin, ouv.pos )
            Some( Subvalidator( desc, ouv, sv ) )

          case _ =>
            // Multiple validators found; this can happen in case of a multiple-clause boolean expression,
            // e.g. "(f1 is notEmpty) or (f2 is notEmpty)".
            Some( BooleanExpression( expr ) )
        }

      case _ => None
    }
  }
}

private class ValidationTransform[ C <: Context, T : C#WeakTypeTag ]( val context: C, v: C#Expr[ T => Unit ] )
  extends ExpressionFinder[ C ] with MacroLogging[ C ] {

  import context.universe._
  import context.abort

  protected val debugOutputEnabled = context.settings.contains( "debugValidationTransform" )
  protected val traceOutputEnabled = context.settings.contains( "traceValidationTransform" )

  val Function( prototype :: prototypeTail, vimpl ) = v.tree
  if ( prototypeTail.nonEmpty )
    abort( prototypeTail.head.pos, "Only single-parameter validators are supported!" )

  // Rewrite expressions into a validation chain --

  /**
   * Each subvalidator of type Validator[ U ] is essentially rewritten as Validator[ T ] via the
   * its extractor; constraint violations are prefixed with the extracted description.
   *
   * @param sv The subvalidator to rewrite
   * @return A valid expression representing a [[com.wix.accord.Validator]] of `T`.
   */
  def rewriteOne( sv: Subvalidator ): Tree = {
    val rewrite =
      q"""
          new com.wix.accord.Validator[ ${weakTypeOf[ T ] } ] {
            def apply( $prototype ) = {
              val sv = ${sv.validation}
              sv( ${sv.ouv} ) withDescription ${sv.description}
            }
          }
       """

    // Report and return the rewritten validator
    debug( s"""|Subvalidator:
               |  Description: ${sv.description}
               |  Validation : ${sv.validation}
               |  Rewrite    : ${show( rewrite )}""".stripMargin, sv.validation.pos )
    trace(    s"  Raw        : ${showRaw( rewrite )}" )
    rewrite
  }

  /** Lifts a multiple-clause boolean expression to a [[com.wix.accord.Validator]] of `T`.
    *
    * Such an expression occurs via [[com.wix.accord.dsl.ValidatorBooleanOps]] of some type `U`, where `U` is the
    * inferred LUB of both clauses; this method lifts the expression to `T` by rewriting the type parameter. This
    * assumes both clauses were previously rewritten (via [[com.wix.accord.transform.ValidationTransform.rewriteOne]].
    *
    * @param tree The tree representing the boolean expression.
    * @return A lifted tree per the description above.
    */
  def liftBooleanOps( tree: Tree ): Tree = {
    val vboTerm = typeOf[ dsl.ValidatorBooleanOps[_] ].typeSymbol.name.toTermName

    transformByPattern( tree ) {
      case Apply( TypeApply( s @ Select( _, `vboTerm` ), _ :: Nil ), e :: Nil ) =>
        Apply( TypeApply( s, TypeTree( weakTypeOf[ T ] ) :: Nil ), liftBooleanOps( e ) :: Nil )
    }
  }

  /** A pattern which rewrites subvalidators found in the tree. */
  val rewriteSubvalidators: TransformAST = {
    case ValidatorApplication( sv: Subvalidator ) =>
      rewriteOne( sv )
  }

  /** A pattern which lifts boolean expressions and rewrites their clauses. */
  val processBooleanExpressions: TransformAST = {
    case ValidatorApplication( BooleanExpression( tree ) ) =>
      liftBooleanOps( transformByPattern( tree )( rewriteSubvalidators ) )
  }

  /** Returns the specified validation block, transformed into a single monolithic validator.
    *
    * @return The transformed [[com.wix.accord.Validator]] of `T`.
    */
  def transformed: Expr[ TransformedValidator[ T ] ] = {
    val subvalidators =
      collectFromPattern( vimpl )( rewriteSubvalidators orElse processBooleanExpressions )
    val result = context.Expr[ TransformedValidator[ T ] ](
      q"new com.wix.accord.transform.ValidationTransform.TransformedValidator( ..$subvalidators )" )

    debug( s"""|Result of validation transform:
               |  Clean: ${show( result )}
               |""".stripMargin )
    trace(   s"|  Raw  : ${showRaw( result )}" )
    result
  }
}

object ValidationTransform {
  // TODO ScalaDocs, and/or find a way to get rid of this!
  class TransformedValidator[ T ]( predicates: Validator[ T ]* ) extends combinators.And[ T ]( predicates:_* ) {
    import scala.language.experimental.macros
    override def compose[ U ]( g: U => T ): Validator[ U ] = macro ValidationTransform.compose[ U, T ]
  }

  def apply[ T : c.WeakTypeTag ]( c: Context )( v: c.Expr[ T => Unit ] ): c.Expr[ TransformedValidator[ T ] ] =
    new ValidationTransform[ c.type, T ]( c, v ).transformed

  def compose[ U : c.WeakTypeTag, T : c.WeakTypeTag ]( c: Context )( g: c.Expr[ U => T ] ): c.Expr[ Validator[ U ] ] = {
    val description = ExpressionDescriber.apply( c )( g )

    import c.universe._
    val rewrite =
     q"""
        new com.wix.accord.Validator[ ${weakTypeOf[ U ]} ] {
          override def apply( v1: ${weakTypeOf[ U ]} ): com.wix.accord.Result =
            ${c.prefix} apply $g( v1 ) withDescription $description
        }
      """

    c.Expr[ Validator[ U ] ]( rewrite )
  }
}
