# Getting started

Accord provides [Specs²](http://www.scalatest.org/) matchers on validation results; the intended use case is to be able to write a test spec for your domain model and its associated validation rules. To enable Specs² integration, add a dependency on the `accord-specs2` module:

## Using sbt

Add the following to your build.sbt or to the settings in your Scala build configuration:

```scala
libraryDependencies += "com.wix" %% "accord-specs2" % "0.4.2" % "test"
```

## Using Maven

Add the following to your POM:

```xml
<dependency>
    <groupId>com.wix</groupId>
    <artifactId>accord-specs2_${scala.tools.version}</artifactId>
    <version>0.4.2</version>
    <scope>test</scope>
</dependency>
```

# API

All matchers are provided by the `com.wix.accord.specs2.ResultMatchers` trait. Simply mix it into your suite or spec as follows:

```scala
import org.specs2.mutable.Specification
import com.wix.accord._
import com.wix.accord.specs2.ResultMatchers

class DomainValidationSpec extends Specification with ResultMatchers {
  // Your tests...
}
```

As an example, consider the following domain model:
```scala
import com.wix.accord.dsl._

object PrimitiveSchema {
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
}
```

All subsequent examples will refer to this domain model.

## Testing success/failure

To test a validation result for success, simply use the provided `succeed` matcher:

```scala
"The Person validator" should {
  "succeed on a valid Person object" in {
    val sample = Person( "Wernher", "von Braun" )
    val result = validate( sample )
    result should succeed
  }
}
```

Likewise, the provided `fail` matcher can be used to ensure that a validation has failed:

```scala
"The Person validator" should {
  "fail on a Person with an empty first name" in {
    val sample = Person( "", "Last name" )
    val result = validate( sample )
    result should fail
  }
}
```

## Testing rule violations

Sometimes merely asserting a validation failure is not enough. Accord also lets you match on the actual violation messages via the `failWith` helper and a simple DSL:

```scala
"The Person validator" should {
  "render a correct violation for a Person with an empty first name" {
    val sample = Person( "", "Last name" )
    val result = validate( sample )
    result should failWith( "firstName" -> "must not be empty" )
  }
}
```

Behind the scenes, the `failWith` helper takes one or more matchers on violations, and wraps them to provide a single matcher on `Result`. The `"firstName" -> "must not be empty"` syntax actually uses an implicit conversion to construct a `RuleViolationMatcher`; the full syntax enables you to explicitly match on any part of the violation:

```scala
val matchByValue       = RuleViolationMatcher( value       = ""                  )
val matchByDescription = RuleViolationMatcher( description = "firstName"         )
val matchByConstraint  = RuleViolationMatcher( constraint  = "must not be empty" )

// You can specify any combination of these requirements, so the following is
// equivalent to the "firstName" -> "must not be empty" syntax:
val expectedViolation = RuleViolationMatcher(
  description = "firstName",
  constraint = "must not be empty" )
```

## Testing group violations

Group violations are relatively rare, and are used to specify that multiple rules have failed with some sort of semantic relation between them. One example is the `Valid` combinator, which enables composition; in the example domain, a `Classroom` is only considered valid if its teacher (a `Person`) is also valid. The `Person` validator defines a number of rules, any of which may be violated by a specific teacher. These violations are all grouped together under a single violation, which you can test with the `group` helper:

```scala
"The Classroom validator" should {
  "fail a classroom with an invalid teacher" in {
    val sample = Classroom( Person( "", "" ), Seq( Person( "Alfred", "Aho" ) ) )
    val result = validate( sample )
    result should failWith( group( "teacher", "is invalid",
      "firstName" -> "must not be empty",
      "lastName" -> "must not be empty"
    ) )
  }
}
```

As with rule violations, the `group` helper simply provides a more concise API to create a `GroupViolationMatcher`. In practice this is equivalent to:

```scala
val expectedViolation = GroupViolationMatcher(
  description = "teacher",
  constraint = "is invalid",
  violations = Seq(
    RuleViolation( description = "firstName", constraint = "must not be empty" ),
    RuleViolation( description = "lastName",  constraint = "must not be empty" ),
  ) )
result should failWith( expectedViolation )
```

