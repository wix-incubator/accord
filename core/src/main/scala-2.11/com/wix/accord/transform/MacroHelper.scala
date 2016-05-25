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
trait MacroHelper[ C <: Context ] {
  self: PatternHelper[ C ] =>

  /** The macro context (of type `C`), must be provided by the inheritor */
  protected val context: C

  import context.universe._
  import org.scalamacros.resetallattrs._

  def termName( symbol: String ): TermName = TermName( symbol )
  def resetAttrs( tree: Tree ): Tree = context.resetAllAttrs( tree )

  def rewriteExistentialTypes( tree: Tree ): Tree = {
    val typeRewrite: PartialFunction[ Tree, Tree ] = {
      // Workaround for https://issues.scala-lang.org/browse/SI-8500. The generated code:
      //
      // [info]     def apply(x$3: Seq[_]) = {
      // [info]       val sv = com.wix.accord.dsl.`package`.Contextualizer[Seq[_$3]](x$3).has.apply(com.wix.accord.dsl.`package`.size.>[Int](0)(math.this.Ordering.Int))(scala.this.Predef.$conforms[Seq[_$3]]);
      // [info]       sv(x$3).withDescription("value")
      // [info]     }
      //
      // Issues the following errors:
      //
      // [error] /Users/tomer/dev/accord/core/src/test/scala/com/wix/accord/tests/dsl/CollectionOpsTests.scala:62: type mismatch;
      // [error]  found   : Seq[_$3(in value sv)] where type _$3(in value sv)
      // [error]  required: Seq[_$3(in value $anonfun)] where type _$3(in value $anonfun)
      // [error]   val seqSizeValidator = validator[ Seq[_] ] { _ has size > 0 }
      case typeTree: TypeTree
        if typeTree.tpe.dealias.typeArgs.nonEmpty && typeTree.tpe.dealias.typeArgs.forall {
          case arg if internal.isSkolem( arg.typeSymbol ) && arg.typeSymbol.asInstanceOf[ TypeSymbolApi ].isExistential => true
          case _ => false
        } =>

        val tpe = typeTree.tpe.asInstanceOf[ TypeRefApi ]
        def existentialTypePara =
          internal.boundedWildcardType( internal.typeBounds( typeOf[ Nothing ], typeOf[ Any ] ) )
        TypeTree( internal.typeRef( tpe.pre, tpe.sym, List.fill( tpe.args.length )( existentialTypePara ) ) )
    }

    transformByPattern( tree )( typeRewrite )
  }
}

object MacroHelper {
  type Context = scala.reflect.macros.blackbox.Context
}
