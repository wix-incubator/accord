/*
  Copyright 2013 Tomer Gabel

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


package com.tomergabel.accord.combinators

import com.tomergabel.accord.{RuleViolation, Validator}

/** Combinators that operate specifically on strings. */
trait StringCombinators {

  /** A validator that succeeds only if the provided string starts with the specified prefix. */
  class StartsWith( prefix: String ) extends Validator[ String ] {
    def apply( x: String ) = result( x startsWith prefix, RuleViolation( x, s"must start with '$prefix'", description ) )
  }
}
