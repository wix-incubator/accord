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

  /**
   * Each validation rule of type Validator[ U ] is essentially rewritten as Validator[ T ] via the
   * its extractor; constraint violations are prefixed with the extracted description.
   *
   * @param rule The validation rule to rewrite
   * @return A valid expression representing a [[com.wix.accord.Validator]] of `T`.
   */
  private def rewriteOne( rule: ValidationRule ): Tree = {
    val description = describeTree( prototype, rule.ouv )
    val rewrite =
      q"""
          new com.wix.accord.Validator[ ${weakTypeOf[ T ] } ] {
            def apply( $prototype ) = {
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

  val processConditionals: TransformAST = {
    case ruleBody @ ConditionalRule( cond, cases ) =>

      val condDescription = describeTree( prototype, cond )
      val rewrittenCases: Seq[ CaseDef ] = cases map {
        case cq"$_ @ _ => default.apply( $_ )" =>
          // Replace synthetic default case with "always succeed"
          cq"_ => com.wix.accord.Success: com.wix.accord.Result"

        case caseDef @ CaseDef( pat, guard, ValidatorApplication( rule: ValidationRule ) ) =>
          val ruleDescription = describeTree( prototype, rule.ouv )
          val guardDescription =
            if ( guard.isEmpty ) q"None" else q"Some( ${ describeTree( prototype, guard ) } )"

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
            def apply( $prototype ) =
              $cond match { case ..$rewrittenCases }
          }
        """
      debug( s"Conditional rule found. Original:\n$ruleBody\n\nRewritten:\n$rewrittenValidator\n\n", ruleBody.pos )
      rewrittenValidator
  }

  /** A pattern which rewrites validation rules found in the tree. */
  val rewriteValidationRules: TransformAST = {
    case ValidatorApplication( sv: ValidationRule ) =>
      rewriteOne( sv )
  }

  /** A pattern which lifts boolean expressions and rewrites their clauses. */
  val processBooleanExpressions: TransformAST = {
    case ValidatorApplication( BooleanExpression( tree ) ) =>
      liftBooleanOps( transformByPattern( tree )( rewriteValidationRules ) )
  }

  /** Returns the specified validation block, transformed into a single monolithic validator.
    *
    * @return The transformed [[com.wix.accord.Validator]] of `T`.
    */
  def transformed: Expr[ TransformedValidator[ T ] ] = {
    val validationRules =
      collectFromPattern( validatorBody )( processConditionals orElse rewriteValidationRules orElse processBooleanExpressions )
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
