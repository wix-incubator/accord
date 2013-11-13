Abstract
========

Accord is a validation library written in and for Scala. Compared to [JSR 303](http://jcp.org/en/jsr/detail?id=303) and [Scalaz validation](https://github.com/scalaz/scalaz/blob/scalaz-seven/core/src/main/scala/scalaz/Validation.scala) it aims to provide the following:

* __Composable__: Because JSR 303 is annotation based, validation rules cannot be composed (annotations cannot receive other annotations as parameters). This is a real problem with some Scala features, for example `Option`s or collections. Accord's validation rules are trivially composable.
* __Simple__: Accord provides a dead-simple story for validation rule definition as well as the validation call site (see example below).
* __Self-contained__: Accord is macro-based but completely self-contained, and consequently only relies on the Scala runtime and reflection libraries.

Accord is work-in-progress and distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0), which basically means you can use and modify it freely. Feedback, bug reports and improvements are welcome!


Example
=======

Importing the library for use:

    import com.tomergabel.accord._


Defining a validator:

    import builder._    // Import the validator DSL

    case class Person( firstName: String, lastName: String )
    case class Classroom( teacher: Person, students: Seq[ Person ] )

    implicit val personValidator = validator[ Person ] { p =>
      p.firstName is notEmpty
      p.lastName is notEmpty
    }

    implicit val classValidator = validator[ Classroom ] { c =>
      c.teacher is valid
      c.students.each is valid
      c.students have size > 0
    }


Running a validator:

    scala> val validPerson = Person( "Wernher", "von Braun" )
    validPerson: Person = Person(Wernher,von Braun)
  
    scala> validate( validPerson )
    res1: com.tomergabel.accord.Result = Success
  
    scala> val invalidPerson = Person( "No Last Name", "" )
    invalidPerson: Person = Person(No Last Name,)
    
    scala> validate( invalidPerson )
    res3: com.tomergabel.accord.Result = Failure(List(Violation(lastName must not be empty,)))

