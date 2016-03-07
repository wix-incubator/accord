package com.wix.accord.transform

import com.wix.accord.transform.MacroHelper._

trait DescriptionRenderer[ C <: Context, Repr ] {
  self: DescriptionModel[ C ] =>

  import context.universe._

  def renderDescription( desc: Description ): Expr[ Repr ]
}

trait StringDescriptionRenderer[ C <: Context ] extends DescriptionRenderer[ C, String ] {
  self: DescriptionModel[ C ] =>

  import context.universe._

  def renderDescription( desc: Description ): Expr[ String ] = context.Expr[ String ]( desc match {
    case ExplicitDescription( tree ) => tree
    case GenericDescription( tree )  => tree
    case SelfReference               => Literal( Constant( "value" ) )
    case AccessChain( elements )     => Literal( Constant( elements.mkString( "." ) ) )
  } )
}
