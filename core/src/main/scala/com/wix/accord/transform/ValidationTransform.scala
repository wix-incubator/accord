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
import com.wix.accord.Descriptions.Description
import com.wix.accord._
import com.wix.accord.transform.ValidationTransform.TransformedValidator

private class ValidationTransform[ C <: Context, T : C#WeakTypeTag ]( val context: C, v: C#Expr[ T => Unit ] )
  extends FunctionDescriber[ C, T, Unit ]
  with RuleFinder[ C ]
  with MacroLogging[ C ] {

  import context.universe._

  protected val debugOutputEnabled = context.settings.contains( "debugValidationTransform" )
  protected val traceOutputEnabled = context.settings.contains( "traceValidationTransform" )

  // Rewrite expressions into a validation chain --

  val ( prototype, validatorBody ) = describeFunction( v in context.mirror )

  type DescriptionTransformation = context.Expr[ Description ] => context.Expr[ Description ]

  def rewriteOne( rule: ValidationRule, transform: DescriptionTransformation = identity ): Tree = {
    val description = transform( describeTree( prototype, rule.ouv ) )
    val rewrite =
      q"""
          new com.wix.accord.Validator[ ${weakTypeOf[ T ] } ] {
            def apply( ${ resetAttrs( prototype.duplicate ) } ) = {
              val validation = ${rule.validation}
              val description = $description
              validation( ${rule.ouv} ) applyDescription description
            }
          }
       """

    // Report and return the rewritten validator
    debug( s"""|Validation rule:
               |  Description: $description
               |  Validation : ${rule.validation}
               |  Rewrite    : ${show( rewrite )}""".stripMargin, rule.validation.pos )
    trace(    s"  Raw        : ${showRaw( rewrite )}" )
    rewrite
  }

  /** Lifts a multiple-clause boolean expression to a [[com.wix.accord.Validator]] of `T`.
    *
    * Such an expression occurs via [[com.wix.accord.dsl.ValidatorBooleanOps]] of some type `U`, where `U` is the
    * inferred LUB of both clauses; this method lifts the expression to `T` by rewriting the type parameter.
    * Additionally, the boolean combinator itself (e.g. [[com.wix.accord.dsl.ValidatorBooleanOps#or]]) takes a
    * type parameter for the right-hand clause, which needs to be lifted to `T`.
    *
    * This assumes both clauses were previously rewritten (via
    * [[com.wix.accord.transform.ValidationTransform.rewriteOne]]).
    *
    * @param tree The tree representing the boolean expression.
    * @return A lifted tree per the description above.
    */
  def liftBooleanOps( tree: Tree ): Tree = {
    val vboTerm = typeOf[ dsl.ValidatorBooleanOps[_] ].typeSymbol.name.toTermName

    transformByPattern( tree ) {
      case TypeApply( Select( Apply( TypeApply( s @ Select( _, `vboTerm` ), _ :: Nil ), e :: Nil ), name ), _ :: Nil ) =>
        val lhs = liftBooleanOps( e )
        val tt = weakTypeOf[ T ]
        val combinator = name.toTermName
        q"com.wix.accord.dsl.ValidatorBooleanOps[ $tt ]( $lhs ).$combinator[ $tt ]"
    }
  }

  // TODO this should probably be elided entirely. Waiting on community feedback
  val processConditionals: TransformAST = {
    case ruleBody @ ConditionalRule( cond, cases ) =>

      val condDescription = describeTree( prototype, cond )
      val rewrittenCases: Seq[ CaseDef ] = cases map {
        case cq"$ignored1 @ _ => default.apply( $ignored2 )" =>   // Macro Paradise for Scala 2.10.x doesn't support $_
          // Replace synthetic default case with "always succeed"
          cq"_ => com.wix.accord.Success: com.wix.accord.Result"

        case caseDef @ CaseDef( _pat, _guard, ValidatorApplication( rule: ValidationRule ) ) =>
          val ruleDescription = describeTree( prototype, rule.ouv )
          val guardDescription =
            if ( _guard.isEmpty ) q"None" else q"Some( ${ describeTree( prototype, _guard ) } )"

          val pat = resetAttrs( _pat.duplicate )
          val guard = resetAttrs( _guard.duplicate )

          val rewrittenBody: CaseDef =
            cq"""
              $pat if $guard =>
                val description = com.wix.accord.Descriptions.Conditional(
                  on = $condDescription,
                  value = $cond,
                  guard = $guardDescription,
                  target = $ruleDescription
                )
                val validation = ${rule.validation}
                validation( ${rule.ouv} ) applyDescription description
            """
          trace( s"Conditional case rewritten. Original:\n$caseDef\n\nRewritten:\n$rewrittenBody\n\n", caseDef.pos )
          rewrittenBody
      }

      val rewrittenValidator =
        q"""
          new com.wix.accord.Validator[ ${weakTypeOf[ T ] } ] {
            def apply( ${ resetAttrs( prototype.duplicate ) } ) =
              $cond match { case ..$rewrittenCases }
          }
        """
      debug( s"Conditional rule found. Original:\n$ruleBody\n\nRewritten:\n$rewrittenValidator\n\n", ruleBody.pos )
      rewrittenValidator
  }

  case class ConditionalBranch( cond: Tree, validator: Tree )
  case class ValidationRuleBranch( branches: Seq[ ConditionalBranch ], default: Option[ Tree ] )

  object Branch {
    def unapply( t: Tree ): Option[ ValidationRuleBranch ] = t match {
      case q"if ( $cond ) { ${ left @ ValidatorApplication(_) }; () } else ${ Branch( right ) }" =>
        Some( right.copy( branches = ConditionalBranch( cond, left ) +: right.branches ) )

      case q"if ( $cond ) { ${ left @ ValidatorApplication(_) }; () } else { ${ right @ ValidatorApplication(_) }; () }" =>
        Some( ValidationRuleBranch( Seq( ConditionalBranch( cond, left ) ), Some( right ) ) )

      case q"if ( $cond ) { ${ opt @ ValidatorApplication(_) }; () } else ()" =>
        // Scala 2.10-style non-terminated if
        Some( ValidationRuleBranch( Seq( ConditionalBranch( cond, opt ) ), None ) )

      case q"if ( $cond ) { ${ opt @ ValidatorApplication(_) }; () }" =>
        // Scala 2.11-style non-terminated if
        Some( ValidationRuleBranch( Seq( ConditionalBranch( cond, opt ) ), None ) )

      case _ =>
        None
    }
  }

  def isWrappedValidationRule( caseDef: CaseDef ): Boolean =
    caseDef.body match {
      case q"{ $rule; () }" if ValidationRule.isValidationRule( rule ) => true
      case q"()" => true  // Default case
      case _ => false
    }

  val processMatches: TransformAST = {
    case t @ Match( rawCond, cases ) if cases forall isWrappedValidationRule =>
      val cond = resetAttrs( rawCond.duplicate )            // Not entirely sure why this is necessary
      val rewrittenBranches = cases collect {
        case CaseDef( pat, guard, q"{ $rule; () }" ) =>
          val guardDescription = if ( guard.isEmpty ) q"scala.None" else q"scala.Some( ${describeTree( prototype, guard )} )"
          val condDescription = describeTree( prototype, cond )
          val description: DescriptionTransformation = { target => context.Expr[ Description ](
            q"""
              com.wix.accord.Descriptions.Conditional(
                on = $condDescription,
                value = $cond,
                guard = $guardDescription,
                target = $target
              )
            """ ) }
          val liftedCondition =
            q"{ ${ resetAttrs( prototype.duplicate) } => $cond match { case $pat if $guard => true; case _ => false } }"
          q"$liftedCondition -> ${ rewriteValidatorApplication( description )( rule ) }"
      }
      val rewrite =
        q"new com.wix.accord.combinators.Conditional[ ${ weakTypeOf[ T ] } ]( Seq( ..$rewrittenBranches ), None )"
      trace( s"After pattern match rewrite:\n$rewrite", pos = t.pos )
      rewrite
  }

  val processBranches: TransformAST = {
    case t @ Branch( ValidationRuleBranch( branches, default ) ) =>
      def describeBranch( cond: Tree ): DescriptionTransformation = {
        val condDescription = describeTree( prototype, cond )
        target: context.Expr[ Description ] => context.Expr[ Description ](
          q"""
              com.wix.accord.Descriptions.Conditional(
                on = com.wix.accord.Descriptions.Generic( "branch" ),
                value = true,
                guard = Some( $condDescription ),
                target = $target
              )
          """
        )
      }

      val rewrittenBranches =
        branches.map { b =>
          val liftedCondition = q"( ${ resetAttrs( prototype.duplicate ) } => ${ resetAttrs( b.cond.duplicate ) } )"
          q"$liftedCondition -> ${ rewriteValidatorApplication( describeBranch( b.cond ) )( b.validator ) }"
        }

      val rewrittenDefault =
        default.map { d =>
          val describeDefault: DescriptionTransformation = { target => context.Expr[ Description ](
            q"""
              com.wix.accord.Descriptions.Conditional(
                on = com.wix.accord.Descriptions.Generic( "branch" ),
                value = false,
                guard = Some( com.wix.accord.Descriptions.Generic( "<else>" ) ),
                target = $target
              )
            """ )
          }
          q"scala.Some( ${ rewriteValidatorApplication( describeDefault )( d ) } )"
        }.getOrElse( q"scala.None" )

      val rewrite =
        q"new com.wix.accord.combinators.Conditional[ ${ weakTypeOf[ T ] } ]( Seq( ..$rewrittenBranches ), $rewrittenDefault )"
      trace( s"After branch rewrite:\n$rewrite", pos = t.pos )
      rewrite
  }

  val processControlStructures: TransformAST = processBranches orElse processMatches orElse processConditionals

  /** A pattern which rewrites validation rules found in the tree. */
  def rewriteValidationRules( transform: DescriptionTransformation = identity ): TransformAST = {
    case ValidatorApplication( sv: ValidationRule ) =>
      rewriteOne( sv, transform )
  }

  /** A pattern which lifts boolean expressions and rewrites their clauses. */
  def processBooleanExpressions( transform: DescriptionTransformation = identity ): TransformAST = {
    case ValidatorApplication( BooleanExpression( tree ) ) =>
      liftBooleanOps( transformByPattern( tree )( rewriteValidationRules( transform ) ) )
  }

  def rewriteValidatorApplication( transform: DescriptionTransformation = identity ) =
    rewriteValidationRules( transform ) orElse processBooleanExpressions( transform )

  /** Returns the specified validation block, transformed into a single monolithic validator.
    *
    * @return The transformed [[com.wix.accord.Validator]] of `T`.
    */
  def transformed: Expr[ TransformedValidator[ T ] ] = {
    val validationRules =
      collectFromPattern( validatorBody )( processControlStructures orElse rewriteValidatorApplication() )
    val result = context.Expr[ TransformedValidator[ T ] ](
      q"new com.wix.accord.transform.ValidationTransform.TransformedValidator( ..$validationRules )" )

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
    val helper = new FunctionDescriber[ c.type, U, T ] {
      val context: c.type = c
      private val ( prototype, body ) = describeFunction( g )
      val description = describeTree( prototype, body )
    }

    import c.universe._
    val rewrite =
     q"""
        new com.wix.accord.Validator[ ${weakTypeOf[ U ]} ] {
          override def apply( v1: ${weakTypeOf[ U ]} ): com.wix.accord.Result =
            ${c.prefix} apply $g( v1 ) applyDescription ${helper.description}
        }
      """

    c.Expr[ Validator[ U ] ]( rewrite )
  }
}
