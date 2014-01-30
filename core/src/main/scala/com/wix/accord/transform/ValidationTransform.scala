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
import com.wix.accord._

private class ValidationTransform[ C <: Context, T : C#WeakTypeTag ]( val context: C, v: C#Expr[ T => Unit ] )
  extends PatternHelper[ C ] with ExpressionDescriber[ C ] {

  import context.universe._
  import context.{abort, info}


  // Macro helpers --

  private val verboseValidatorRewrite = context.settings.contains( "verboseValidationTransform" )
  def log( s: String, pos: Position = context.enclosingPosition ) =
    if ( verboseValidatorRewrite ) info( pos, s, force = false )


  // Transformation logic --

  val Function( prototype :: prototypeTail, vimpl ) = v.tree
  if ( !prototypeTail.isEmpty )
    abort( prototypeTail.head.pos, "Only single-parameter validators are supported!" )

  case class Subvalidator( description: Tree, ouv: Tree, validation: Tree )

  val validatorType = typeOf[ Validator[_] ]

  /** An extractor for validation rules. The object under validation is, by design, wrapped in the implicit
    * DSL construct [[com.wix.accord.dsl.Contextualizer]], so that a validation rule can be defined with
    * syntax like `p.firstName is notEmpty`.
    *
    * In the example above, `p.firstName` is the expression wrapped by [[com.wix.accord.dsl.Contextualizer]]
    * and yields the Object Under Validation (OUV).
    */
  object ValidatorApplication {
    private val contextualizerTerm = typeOf[ dsl.Contextualizer[_] ].typeSymbol.name.toTermName

    def extractObjectUnderValidation( t: Tree ) =
      extractFromPattern( t ) {
        case Apply( TypeApply( Select( _, `contextualizerTerm` ), tpe :: Nil ), e :: Nil ) =>
          ( context.resetAllAttrs( e.duplicate ), tpe.tpe )
      } getOrElse
        abort( t.pos, s"Failed to extract object under validation from tree $t (raw=${showRaw(t)})" )

    def rewriteContextExpressionAsValidator( expr: Tree, extractor: Tree ) =
      transformByPattern( expr ) {
        case Apply( t @ TypeApply( Select( _, `contextualizerTerm` ), _ ), e :: Nil ) =>
          Apply( t, extractor :: Nil )
      }

    def unapply( expr: Tree ): Option[ Subvalidator ] = expr match {
      case t if t.tpe <:< validatorType =>
        val ( ouv, ouvtpe ) = extractObjectUnderValidation( expr )
        val sv = rewriteContextExpressionAsValidator( expr, ouv )
        val desc = renderDescriptionTree( ouv )
        log( s"""
              |Found subvalidator:
              |  ouv=$ouv
              |  ouvraw=${showRaw(ouv)}
              |  ouvtpe=$ouvtpe
              |  sv=${show(sv)}
              |  svraw=${showRaw(sv)}
              |  desc=$desc
              |""".stripMargin, ouv.pos )
        Some( Subvalidator( desc, ouv, sv ) )

      case _ => None
    }
  }

  def findSubvalidators( t: Tree ): List[ Subvalidator ] = t match {
    case Block( stats, expr ) => ( stats flatMap findSubvalidators ) ++ findSubvalidators( expr )
    case ValidatorApplication( validator ) => validator :: Nil
    case Literal( Constant(()) ) => Nil   // Ignored terminator
    case _ => abort( t.pos, s"Unexpected node $t:\n\ttpe=${t.tpe}\n\traw=${showRaw(t)}" )
  }

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
    log( s"""|Subvalidator:
               |  Description: ${sv.description}
               |  Validation : ${sv.validation}
               |
               |Rewritten as:
               |  Clean      : ${show( rewrite )}
               |  Raw        : ${showRaw( rewrite )}
               |""".stripMargin, sv.validation.pos )
    rewrite
  }

  /** Returns the specified validation block, transformed into a single monolithic validator.
    *
    * @return The transformed [[com.wix.accord.Validator]] of `T`.
    */
  def transformed: Expr[ Validator[ T ] ] = {
    // Rewrite all validators
    val subvalidators = findSubvalidators( vimpl ) map rewriteOne
    val result = context.Expr[ Validator[ T ] ]( q"new com.wix.accord.combinators.And( ..$subvalidators )" )

    log( s"""|Result of validation transform:
             |  Clean: ${show( result )}
             |  Raw  : ${showRaw( result )}
             |""".stripMargin )
    result
  }
}

object ValidationTransform {
  def apply[ T : c.WeakTypeTag ]( c: Context )( v: c.Expr[ T => Unit ] ): c.Expr[ Validator[ T ] ] =
    new ValidationTransform[ c.type, T ]( c, v ).transformed
}
