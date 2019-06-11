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
import com.wix.accord.Descriptions.{Branch, Path}
import com.wix.accord._
import com.wix.accord.transform.ValidationTransform.TransformedValidator

private class ValidationTransform[ C <: Context, T : C#WeakTypeTag ]( val context: C, v: C#Expr[ T => Unit ] )
  extends FunctionDescriber[ C, T, Unit ]
  with RuleFinder[ C ]
  with MacroLogging[ C ] {

  import context.universe._

  protected val debugOutputEnabled: Boolean = context.settings.contains( "debugValidationTransform" )
  protected val traceOutputEnabled: Boolean = context.settings.contains( "traceValidationTransform" )

  // Rewrite expressions into a validation chain --

  val ( prototype, validatorBody ) = describeFunction( v in context.mirror )

  type PathTransformation = context.Expr[ Path ] => context.Expr[ Path ]

  def rewriteOne( rule: ValidationRule, transform: PathTransformation = identity ): Tree = {
    val description = transform( rule.path )
    val rewrite =
      q"""
          new com.wix.accord.Validator[ ${weakTypeOf[ T ] } ] {
            import com.wix.accord.DescriptionBuilders._

            def apply( ${ resetAttrs( prototype.duplicate ) } ) = {
              val validation$$0 = ${rule.validation}
              val path$$0 = $description
              validation$$0( ${rule.ouv} ) prepend path$$0
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

  case class ConditionalBranch( cond: Tree, rules: Seq[ Tree ] )
  case class ValidationRuleBranch( branches: Seq[ ConditionalBranch ], default: Option[ Tree ] )

  object ValidatorApplicationBlock {
    def unapply( t: Tree ): Option[ Seq[ Tree ] ] = t match {
      case Block( rules, q"()" ) if rules forall ValidatorApplication.isValid =>
        Some( rules )

      case Block( rules, terminal ) if rules.forall( ValidatorApplication.isValid ) && ValidatorApplication.isValid( terminal ) =>
        Some( rules :+ terminal )

      case single @ ValidatorApplication(_) =>
        // Because ValidatorApplication will match a two-statement validation block, it must be last on this list.
        // TODO that branch of ValidatorApplication is intended for boolean clauses; perhaps it can be tightened
        Some( Seq( single ) )

      case _ => None
    }
  }

  object Branch {
    def unapply( t: Tree ): Option[ ValidationRuleBranch ] = t match {
      case q"if ( $cond ) ${ ValidatorApplicationBlock( left ) } else ${ Branch( right ) }" =>
        Some( right.copy( branches = ConditionalBranch( cond, left ) +: right.branches ) )

      case q"if ( $cond ) ${ ValidatorApplicationBlock( left ) } else ${ right @ ValidatorApplicationBlock(_) }" =>
        Some( ValidationRuleBranch( Seq( ConditionalBranch( cond, left ) ), Some( right ) ) )

      case q"if ( $cond ) ${ ValidatorApplicationBlock( rules ) }" =>
        Some( ValidationRuleBranch( Seq( ConditionalBranch( cond, rules ) ), None ) )

      case _ =>
        None
    }
  }

  val processMatches: TransformAST = {
    case t @ Match( rawCond, cases ) =>
      val cond = resetAttrs( rawCond.duplicate )
      val rewrittenBranches = cases collect {
        case CaseDef( pat, guard, ValidatorApplicationBlock( rules ) ) =>
          val guardDescription =
            if ( guard.isEmpty ) q"scala.None" else q"scala.Some( ${genericDescription( guard )} )"
          val condDescription = describeTree( prototype, cond )
          val description: PathTransformation = { target => context.Expr[ Path ](
            q"""
              com.wix.accord.Descriptions.PatternMatch(
                on = $condDescription,
                value = $cond,
                guard = $guardDescription
              ) +: $target
            """ ) }

          val rewriteOne = rewriteValidatorApplication( description )
          val aggregate = rules match {
            case Nil =>
              context.abort( t.pos, "Unexpected rule count (safety net; should never happen)" )
            case head +: Nil =>
              rewriteOne( head )
            case _ =>
              val rewrittenRules = rules map rewriteOne
              q"new com.wix.accord.combinators.And[ ${ weakTypeOf[ T ] } ]( ..$rewrittenRules )"
          }

          val liftedCondition =
            q"{ ${ resetAttrs( prototype.duplicate) } => $cond match { case $pat if $guard => true; case _ => false } }"
          q"$liftedCondition -> $aggregate"
      }
      val rewrite =
        q"new com.wix.accord.combinators.Conditional[ ${ weakTypeOf[ T ] } ]( Seq( ..$rewrittenBranches ), None )"
      trace( s"After pattern match rewrite:\n$rewrite", pos = t.pos )
      rewrite
  }

  val processBranches: TransformAST = {
    case t @ Branch( ValidationRuleBranch( branches, default ) ) =>
      def describeBranch( cond: Tree, evaluation: Boolean ): PathTransformation = {
        val condDescription = genericDescription( cond )
        target: context.Expr[ Path ] =>
          context.Expr[ Path ]( q"com.wix.accord.Descriptions.Branch( $condDescription, $evaluation ) +: $target" )
      }

      val rewrittenBranches =
        branches.map { b =>
          val liftedCondition = q"( ${ resetAttrs( prototype.duplicate ) } => ${ resetAttrs( b.cond.duplicate ) } )"
          val rewriteOne = rewriteValidatorApplication( describeBranch( b.cond, evaluation = true ) )
          val aggregate = b.rules match {
            case Nil =>
              context.abort( t.pos, "Unexpected rule count (safety net; should never happen)" )
            case head +: Nil =>
              rewriteOne( head )
            case rules =>
              val rewrittenRules = rules map rewriteOne
              q"new com.wix.accord.combinators.And[ ${ weakTypeOf[ T ] } ]( ..$rewrittenRules )"
          }
          q"$liftedCondition -> $aggregate"
        }

      val rewrittenDefault =
        default.map { d =>
          val describeDefault = describeBranch( branches.last.cond, evaluation = false )
          q"scala.Some( ${ rewriteValidatorApplication( describeDefault )( d ) } )"
        }.getOrElse( q"scala.None" )

      val rewrite =
        q"new com.wix.accord.combinators.Conditional[ ${ weakTypeOf[ T ] } ]( Seq( ..$rewrittenBranches ), $rewrittenDefault )"
      trace( s"After branch rewrite:\n$rewrite", pos = t.pos )
      rewrite
  }

  val processControlStructures: TransformAST = processBranches orElse processMatches

  /** A pattern which rewrites validation rules found in the tree. */
  def rewriteValidationRules( transform: PathTransformation = identity ): TransformAST = {
    case ValidatorApplication( sv: ValidationRule ) =>
      rewriteOne( sv, transform )
  }

  /** A pattern which lifts boolean expressions and rewrites their clauses. */
  def processBooleanExpressions( transform: PathTransformation = identity ): TransformAST = {
    case ValidatorApplication( BooleanExpression( tree ) ) =>
      liftBooleanOps( transformByPattern( tree )( rewriteValidationRules( transform ) ) )
  }

  def rewriteValidatorApplication( transform: PathTransformation = identity ): TransformAST =
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
          import com.wix.accord.DescriptionBuilders._

          override def apply( v1: ${weakTypeOf[ U ]} ): com.wix.accord.Result =
            ${c.prefix} apply $g( v1 ) prepend ${helper.description}
        }
      """

    c.Expr[ Validator[ U ] ]( rewrite )
  }
}
