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
trait MacroHelper[ C <: Context ] {
  /** The macro context (of type `C`), must be provided by the inheritor */
  protected val context: C

  import context.universe._

  def termName( symbol: String ): TermName = TermName( symbol )
  def resetAttrs( tree: Tree, repairOwners: Boolean = true ): Tree = {
    val afterReset = context.untypecheck( tree )
    if ( repairOwners ) {
      val or = new OwnerRepair( context )
      or.repairOwners( afterReset.asInstanceOf[ or.c.universe.Tree ] ).asInstanceOf[ Tree ]
    } else afterReset
  }
}

// Workaround for SI-5797, copied from Jason Zaugg's comment at https://issues.scala-lang.org/browse/SI-5797
// This is needed to repair owner chain as encountered in the following issue:
// https://github.com/scalatest/scalatest/issues/276
private class OwnerRepair[C <: Context](val c: C) {
  /**
   * If macro arguments are spliced into underneath DefTree that introduces
   * an entry into the symbol ownership chain, any symbols defined in the
   * spliced tree will be ill-owned.
   *
   * This method detects this situation, and corrects the owners.
   */
  def repairOwners(tree: c.Tree): c.Tree = {
    val symtab = c.universe.asInstanceOf[reflect.internal.SymbolTable]
    val utils = new Utils[symtab.type](symtab)

    println (s"about to typecheck: ${c.universe.showCode(tree)}")

    // Proactively typecheck the tree. This will assign symbols to
    // DefTrees introduced by the macro.
    val typed = c.typecheck(tree).asInstanceOf[symtab.Tree]

    println (s"after typecheck: ${c.universe.showCode(typed.asInstanceOf[c.Tree])}")

    // The current owner at the call site. Symbols owned by this may need
    // to be transplanted.
    import scala.reflect.macros.runtime.{Context => MRContext}
    val callsiteOwner =
      c.asInstanceOf[MRContext]
        .callsiteTyper.context.owner
        .asInstanceOf[symtab.Symbol]

    val repairedTree = utils.repairOwners(typed, callsiteOwner).asInstanceOf[c.universe.Tree]
    println (s"after repair: ${c.universe.showCode(repairedTree)}")
    repairedTree
  }

  private class Utils[U <: reflect.internal.SymbolTable](val u: U) {
    import u._

    class ChangeOwnerAndModuleClassTraverser(oldowner: Symbol, newowner: Symbol)
      extends ChangeOwnerTraverser(oldowner, newowner) {

      override def traverse(tree: Tree) {
        tree match {
          case _: DefTree => change(tree.symbol.moduleClass)
          case _          =>
        }
        super.traverse(tree)
      }
    }

    def repairOwners(t: Tree, macroCallSiteOwner: Symbol): Tree = {
      object repairer extends Transformer {
        override def transform(t: Tree): Tree = {
          t match {
            case (_: DefTree | _: Function | _: Import) if t.symbol.owner == macroCallSiteOwner && macroCallSiteOwner != currentOwner =>
              new ChangeOwnerAndModuleClassTraverser(macroCallSiteOwner, currentOwner)(t)
            case _ =>
              super.transform(t)
          }
        }
      }
      repairer.atOwner(macroCallSiteOwner) {
        repairer transform t
      }
    }
  }
}

object MacroHelper {
  type Context = scala.reflect.macros.blackbox.Context
}
