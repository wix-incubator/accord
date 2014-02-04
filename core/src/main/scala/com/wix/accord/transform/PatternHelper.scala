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

// TODO ScalaDocs
trait PatternHelper[ C <: Context ] {
  val context: C

  import context.universe._

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

  def transformByPattern( tree: Tree )( pattern: PartialFunction[ Tree, Tree ] ): Tree = {
    val transformed =
      new Transformer {
        override def transform( subtree: Tree ): Tree =
          if ( pattern isDefinedAt subtree ) pattern.apply( subtree ) else super.transform( subtree )
      }.transform( tree.duplicate )
    context.resetAllAttrs( transformed )
  }
}
