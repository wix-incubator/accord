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

package com.tomergabel.accord

object PrimitiveSchema {
  import dsl._

  case class Person( firstName: String, lastName: String )
  case class Classroom( teacher: Person, students: Seq[ Person ] )

  implicit val personValidator = validator[ Person ] { p =>
    p.firstName is notEmpty
    p.lastName is notEmpty
    p.firstName.length is > 5
  }

  implicit val classValidator = validator[ Classroom ] { c =>
    c.teacher is valid
    c.students.each is valid
    c.students have size > 0
  }
}

