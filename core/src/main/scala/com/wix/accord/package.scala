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

package com.wix

import com.wix.accord.simple.SimpleDomain

/** The entry-point to the Accord library. To execute a validator, simply import it into the local scope,
  * import this package and execute `validate( objectUnderValidation )`.
  *
  * @deprecated Starting with Accord 0.5, you should explicitly import the desired constraint model, e.g.
  *             `import com.wix.accord.simple._` (which has the same behavior as pre-0.5 versions). This is provided
  *             for backwards compatibility only, and will be removed by 0.6.
  */
package object accord extends SimpleDomain
