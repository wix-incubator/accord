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

/** A macro helper mixin that provides simplified, pattern-based AST operations.
  *
  * @tparam C The type of the macro context
  */
trait PatternHelper[ C <: Context ] {
  self: MacroHelper[ C ] =>

  import context.universe._

  implicit class ExtendType( tpe: Type ) {
    def isNull =
      // Workaround to correctly deal with constant types (e.g. null literals), for more details see:
      // https://groups.google.com/forum/#!topic/scala-user/hGrGmGk4b88
      tpe.widen =:= typeOf[ Null ]

    def isBottom =
      tpe =:= typeOf[ Nothing ] || isNull

    def =!=( that: Type ): Boolean = {
      if ( that == typeOf[ Null ] )
        !isNull
      else
        !( tpe =:= that )
    }
  }

  /** Matches an AST pattern against a tree recursively. Patterns are encoded as a partial function from
    * [[scala.reflect.api.Universe.Tree]] to a result object; this method returns the result of applying the partial
    * function to the first AST subtree matching it.
    *
    * @param tree The AST tree to search.
    * @param pattern The search pattern.
    * @tparam R The return type as defined by the extraction clause of the search pattern.
    * @return [[scala.None]] if no match was found, or a [[scala.Some]] containing the result of applying the
    *         partial function to the first matching subtree.
    */
  def extractFromPattern[ R ]( tree: Tree )( pattern: PartialFunction[ Tree, R ] ): Option[ R ] = {
    var found: Option[ R ] = None
    new Traverser {
      override def traverse( subtree: Tree ) {
        if ( pattern.isDefinedAt( subtree ) )
          found = Some( pattern( subtree ) )
        else
          super.traverse( subtree )
      }
    }.traverse( tree )
    found
  }

  /** Matches an AST pattern against a tree recursively. Patterns are encoded as a partial function from
    * [[scala.reflect.api.Universe.Tree]] to a result object; this method returns the result of applying the partial
    * function to all AST subtrees matching it.
    *
    * @param tree The AST tree to search.
    * @param pattern The search pattern.
    * @tparam R The return type as defined by the extraction clause of the search pattern.
    * @return [[scala.None]] if no match was found, or a [[scala.Some]] containing the result of applying the
    *         partial function to the first matching subtree.
    */
  def collectFromPattern[ R ]( tree: Tree )( pattern: PartialFunction[ Tree, R ] ): List[ R ] = {
    var found: Vector[ R ] = Vector.empty
    new Traverser {
      override def traverse( subtree: Tree ) {
        if ( pattern.isDefinedAt( subtree ) )
          found = found :+ pattern( subtree )
        else
          super.traverse( subtree )
      }
    }.traverse( tree )
    found.toList
  }

  type TransformAST = PartialFunction[ Tree, Tree ]

  /** Transforms an AST based on the specified pattern. The transformation is specified as a partial function from
    * [[scala.reflect.api.Universe.Tree]] to a another tree, where every subtree for which the function is defined
    * is replaced with the result of its application.
    *
    * @param tree The AST tree to search.
    * @param pattern The search-and-replace pattern.
    * @return The transformed tree.
    */
  def transformByPattern( tree: Tree )( pattern: TransformAST ): Tree = {
    val transformed =
      new Transformer {
        override def transform( subtree: Tree ): Tree =
          if ( pattern isDefinedAt subtree ) pattern.apply( subtree ) else super.transform( subtree )
      }.transform( tree.duplicate )
    resetAttrs( transformed )
  }
}
