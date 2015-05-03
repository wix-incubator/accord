[![Build Status](https://travis-ci.org/wix/accord.png?branch=master)](https://travis-ci.org/wix/accord) [![Coverage Status](https://coveralls.io/repos/wix/accord/badge.png?branch=master)](https://coveralls.io/r/wix/accord?branch=master) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/wix/accord?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)


Overview
========

Accord is a validation library written in and for Scala. Compared to [JSR 303](http://jcp.org/en/jsr/detail?id=303) and [Scalaz validation](https://github.com/scalaz/scalaz/blob/scalaz-seven/core/src/main/scala/scalaz/Validation.scala) it aims to provide the following:

* __Composable__: Because JSR 303 is annotation based, validation rules cannot be composed (annotations cannot receive other annotations as parameters). This is a real problem with some Scala features, for example `Option`s or collections. Accord's validation rules are trivially composable.
* __Simple__: Accord provides a dead-simple story for validation rule definition by leveraging macros, as well as the validation call site (see example below).
* __Self-contained__: Accord is macro-based but completely self-contained, and consequently only relies on the Scala runtime and reflection libraries.
* __Integrated__: Other than providing its own DSL and matcher library, Accord is designed to easily integrate with other libraries, and provides out-of-the-box integration with [Spring Validation](https://github.com/wix/accord/wiki/Spring-Integration), [Specs<sup>2</sup>](https://github.com/wix/accord/wiki/Specs%C2%B2-Integration) and [ScalaTest](https://github.com/wix/accord/wiki/ScalaTest-Integration).

Accord is developed and used at [![Wix.com Logo](wix_logo.png)](http://www.wix.com) and distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0), which basically means you can use and modify it freely. Feedback, bug reports and improvements are welcome!

Example
=======

Importing the library for use:

```scala
import com.wix.accord._
```

Defining a validator:

```scala
import dsl._    // Import the validator DSL

case class Person( firstName: String, lastName: String )
case class Classroom( teacher: Person, students: Seq[ Person ] )

implicit val personValidator = validator[ Person ] { p =>
  p.firstName is notEmpty                   // The expression being validated is resolved automatically, see below
  p.lastName as "last name" is notEmpty     // You can also explicitly describe the expression being validated
}

implicit val classValidator = validator[ Classroom ] { c =>
  c.teacher is valid        // Implicitly relies on personValidator!
  c.students.each is valid
  c.students have size > 0
}
```


Running a validator:

```
scala> val validPerson = Person( "Wernher", "von Braun" )
validPerson: Person = Person(Wernher,von Braun)

scala> validate( validPerson )
res0: com.wix.accord.Result = Success

scala> val invalidPerson = Person( "", "No First Name" )
invalidPerson: Person = Person(,No First Name)

scala> validate( invalidPerson )
res1: com.wix.accord.Result = Failure(List(RuleViolation(,must not be empty,firstName)))

scala> val explicitDescription = Person( "No Last Name", "" )
explicitDescription: Person = Person(No Last Name,)

scala> validate( explicitDescription )
res2: com.wix.accord.Result = Failure(List(RuleViolation(,must not be empty,last name)))

scala> val invalidClassroom = Classroom( Person( "Alfred", "Aho" ), Seq.empty )
invalidClassroom: Classroom = Classroom(Person(Alfred,Aho),List())

scala> validate( invalidClassroom )
res3: com.wix.accord.Result = Failure(List(RuleViolation(List(),has size 0, expected more than 0,students)))
```

Getting Started
===============

Accord version 0.4.2 is available on Maven Central Repository. Scala versions 2.10.3+ and 2.11.x are supported. The next milestone is 0.5-SNAPSHOT and is available from the Sonatype snapshots repository.

SBT
---

Simply add the `accord-core` module to your build settings:

```scala
libraryDependencies += "com.wix" %% "accord-core" % "0.4.2"
```

If you want to evaluate the upcoming snapshot release, add the Sonatype snapshot repository to your resolvers; typically this means adding the following to your `build.sbt` file:

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "com.wix" %% "accord-core" % "0.5-SNAPSHOT"
```

Maven
-----

Accord is published to the Maven Central Repository, so you simply have to add the appropriate dependency to your POM:

```xml
<dependencies>
  <dependency>
    <groupId>com.wix</groupId>
    <artifactId>accord-core_${scala.tools.version}</artifactId>
    <version>0.4.2</version>
  </dependency>
</dependencies>
```

If you want to evaluate the upcoming snapshot release, add the Sonatype snapshot repository and a dependency on Accord to your POM:

```xml
<repositories>
  <repository>
    <id>sonatype-snapshots</id>
    <name>Sonatype snapshot repository</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <releases><enabled>false</enabled></releases>
    <snapshots><enabled>true</enabled></snapshots>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.wix</groupId>
    <artifactId>accord-core_${scala.tools.version}</artifactId>
    <version>0.5-SNAPSHOT</version>
  </dependency>
</dependencies>
```

Roadmap
=======

Accord is still fairly rudimentary, and there's plenty of improvements to be made:

* Major issues and improvements planned or under consideration for the [0.5 milestone](https://github.com/wix/accord/issues?milestone=5&state=open) release:
    * [#6](https://github.com/wix/accord/issues/6) Support inline expressions in validators
    * [#19](https://github.com/wix/accord/issues/19) Scala.js support
    * [#21](https://github.com/wix/accord/issues/21) i18n support
    * Rethink binary expression API so that arbitrary types can be used
* Future plans:
    * Elide DSL implicit invocations from resulting tree for better performance and smaller code
    * Add syntax for custom violation message overrides (e.g. `p.firstName is notEmpty as "no first name!"`)
    * Support custom violation types (e.g. `p.firstName is notEmpty as MyServerError( code = -3 )`)
    * Add a validation result rendering framework
    * Implement accord-hamcrest integration in a separate artifact
    * Add adapter for Specs² matcher module

Ideas and feature requests welcome!

