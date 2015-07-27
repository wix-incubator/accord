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

package com.wix.accord.spring

import scala.reflect.ClassTag
import com.wix.accord.Validator
import java.lang.reflect.{ParameterizedType, Method}

/** A resolver that takes a class and returns its respective [[com.wix.accord.Validator]]. */
trait AccordValidatorResolver {

  /** Takes a class and returns a [[com.wix.accord.Validator]], if available.
    *
    * @tparam T The type representing the class under validation.
    * @return [[scala.Some]] validator of type `T`, or [[scala.None]] if no suitable validator could be resolved.
    */
  def lookupValidator[ T : ClassTag ]: Option[ Validator[ T ] ]
}

/** A resolver that looks up validator definitions in the companion object of the class under validation. */
class CompanionObjectAccordValidatorResolver extends AccordValidatorResolver {
  // TODO memoize companion lookup

  /** Takes a class and returns its companion object, if available.
    *
    * @param clazz The class for which a companion object is required.
    * @return A [[scala.Some]] containing the companion object, or [[scala.None]] if unavailable.
    */
  private def companionOf( clazz: Class[_] ): Option[ AnyRef ] =
    try {
      val companionClassName = clazz.getName + "$"
      val companionClass = Class.forName( companionClassName )
      val moduleField = companionClass.getField( "MODULE$" )
      Some( moduleField.get( null ) )
    } catch {
      case _: Exception => None
    }

  /** An extractor object which matches [[java.lang.reflect.Method]]s with generic return types. */
  private object GenericReturnType {
    def unapply( m: Method ): Option[( Class[_], List[ Option[ Class[_] ] ] )] = m match {
      case _ if m.getGenericReturnType.isInstanceOf[ ParameterizedType ] =>
        val tpe = m.getGenericReturnType.asInstanceOf[ ParameterizedType ]
        val typeConstructor = tpe.getRawType.asInstanceOf[ Class[_] ]
        val typeArguments =
          tpe.getActualTypeArguments.toList map {
            case ta: Class[_] => Some( ta )
            case _ => None
          }
        Some( typeConstructor, typeArguments )
      case _ => None
    }
  }

  def lookupValidator[ T : ClassTag ]: Option[ Validator[ T ] ] = {
    val clazz = implicitly[ ClassTag[ T ] ].runtimeClass
    companionOf( clazz ) flatMap { companion =>
      companion.getClass.getMethods collectFirst {
        case m @ GenericReturnType( tpe, Some( of ) :: Nil )
          if classOf[ Validator[_] ].isAssignableFrom( tpe ) && clazz.isAssignableFrom( of ) =>
          m.invoke( companion ).asInstanceOf[ Validator[ T ] ]
      }
    }
  }
}
