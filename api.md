---
layout: page
title: "The Accord API"
---

# Overview

Accord's API comprises three main building blocks:

* [Execution](#execution) and discovery;
* A result [domain model](#result-model);
* A dedicated [DSL](dsl.html) and [combinator library](dsl.html#combinator-library) for defining validation rules.

# Execution
<a name="execution"></a>

As with any validation framework, Accord faces two design considerations: discovery (which validator should be executed? What happens if there are multiple options?) and execution (running the actual validator and obtaining the results). Accord solves these dilemmas by:
  
* Defining `Validator[T]` as a function from some type under validation `T` to Accord's [result model](#result-model), and
* Making `Validator[T]` into a typeclass, and dealing with discovery using Scala's normal implicit resolution mechanism.

This provides a very flexible API with which to scope and execute validators. The recommended practice is to place a class's validator in its companion object, thereby making it automatically visible to anyone using the class, but validators can be placed anywhere and resolved explicitly:

```scala
case class Person( name: String, age: Int )
case object Person {
  implicit com.wix.accord.dsl._

  // The following validator is automatically included in implicit search scope,
  // so users of the Person class do not have to explicitly import it.
  implicit val personValidator = validator[ Person ] { p =>
    p.name is notEmpty
    p.age should be >= 18
  }
}


import com.wix.accord._       // Import the Accord root API

val person = Person( "Sherlock Holmes", 27 )

// A suitable validator is automatically resolved from Person's companion object:
val result1 = validate( person )
// You can also specify an explicit validator:
val result2 = validate( person )( Person.personValidator )
// Since a validator is a function, you can also explicitly apply it:
val result3 = Person.personValidator( person )

// All three are equivalent!
assert( result1 == result2 && result2 == result3 )
```

# Results
<a name="result-model"></a>

The Accord domain model is essentially quite simple (this is a simplified excerpt, for the full API definition see [Result.scala](https://github.com/wix/accord/blob/v{{ site.version.release }}/api/src/main/scala/com/wix/accord/Result.scala)):

```scala
trait Violation {
  /** The actual value that failed validation, e.g. 15 */
  def value: Any                     
  /** The violated constraint, e.g. "got 15, expected 18 or more" */
  def constraint: String
  /** A textual representation of the expression that failed validation */
  def description: Option[ String ]
}

sealed trait Result
case object Success extends Result
case class Failure( violations: Set[ Violation ] ) extends Result
```

The vast majority of violations indicate a broken *rule*, but some validators (notably delegation and logical OR) produce a violation that encompasses a *group* of violations. Consider the following example:

```scala
case class Address( street: String, city: String, zipcode: Option[ String ] )
case class Person( name: String, age: Int, address: Address )

import com.wix.accord.dsl._

implicit val addressValidator = validator[ Address ] { a =>
  a.street is notEmpty
  a.city is notEmpty
  a.zipcode.each is notEmpty
}

implicit val personValidator = validator[ Person ] { p =>
  p.name is notEmpty
  p.age should be >= 18
  p.address is valid      // Implicitly delegates to addressValidator
}
```

What happens if you validate an instance of `Person` that has an empty name? A *rule* has been broken, and consequently you'd expect a single violation. But what if the person's address is also bad, and in fact has multiple violations? One can imagine two possibilities:

* Generate multiple violations, and mark each with a "path" to the relevant expression (e.g. `p.address.street`). This has the benefit of only requiring a single abstraction, but becomes quite unwieldy with multiple indirections, indexing into sequences and other "path" types.
* Add a container type to the domain model. While it makes the model more complex, it has the benefit of added flexibility on top of the full context.

Accord takes the second approach, and provides two built-in violation types: `RuleViolation` and `GroupViolation` (see [full API](https://github.com/wix/accord/blob/v{{ site.version.release }}/api/src/main/scala/com/wix/accord/Result.scala)). Based on the above example, here's a sample validation output:

```scala

import com.wix.accord._
val result = validate( Person( "", 27, Address( "221B Baker Street", "", Some( "" ) ) ) )

// Results in:
result ==
  Failure( Set(
    // First violation:
    RuleViolation( "", "must not be empty", Some( "name" ) ),

    // Second violation:
    GroupViolation(
      Address( "221B Baker Street", "", Some( "" ) ),
      "is invalid",
      Some( "address" ),   // The violation applies to the "address" field
      Set(                 // ... and may include multiple clauses:
        RuleViolation( "", "must not be empty", Some( "city" ) ),
        RuleViolation( "", "must not be empty", Some( "zipcode" ) )
      )
    )
  ) )
```
