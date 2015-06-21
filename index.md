---
layout: page
title: "A sane validation library for Scala"
---

Overview
========

Accord is a validation library written in and for Scala. Compared to [JSR 303](http://jcp.org/en/jsr/detail?id=303) and [Scalaz validation](https://github.com/scalaz/scalaz/blob/scalaz-seven/core/src/main/scala/scalaz/Validation.scala) it aims to provide the following:

* __Composable__: Because JSR 303 is annotation based, validation rules cannot be composed (annotations cannot receive other annotations as parameters). This is a real problem with some Scala features, for example `Option`s or collections. Accord's validation rules are trivially composable.
* __Simple__: Accord provides a dead-simple story for validation rule definition by leveraging macros, as well as the validation call site (see example below).
* __Self-contained__: Accord is macro-based but completely self-contained, and consequently only relies on the Scala runtime and reflection libraries.
* __Integrated__: Other than providing its own DSL and matcher library, Accord is designed to easily integrate with other libraries, and provides out-of-the-box integration with [Spring Validation](spring3/README.md), [Specs<sup>2</sup>](specs2/README.md) and [ScalaTest](scalatest/README.md).

Accord is developed and used at <a href="http://www.wix.com"><img src="images/wix_logo.png" width="42" height="11" alt="Wix.com"></img></a> and distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0), which basically means you can use and modify it freely. Feedback, bug reports and improvements are welcome!

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

<a name="getting-started"></a>
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

