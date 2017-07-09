---
layout: page
title: "A sane validation library for Scala"
---

Overview
========

Accord is a data validation library written in and for Scala. Compared to [JSR 303](http://jcp.org/en/jsr/detail?id=303) and [Scalaz validation](https://github.com/scalaz/scalaz/blob/scalaz-seven/core/src/main/scala/scalaz/Validation.scala) it aims to provide the following:

* __Composable__: Because JSR 303 is annotation based, validation rules cannot be composed (annotations cannot receive other annotations as parameters). This is a real problem with some Scala features, for example `Option`s or collections. Accord's validation rules are trivially composable.
* __Simple__: Accord provides a dead-simple story for validation rule definition by leveraging macros, as well as the validation call site (see example below).
* __Self-contained__: Accord is macro-based but completely self-contained, and consequently only relies on the Scala runtime and reflection libraries.
* __Integrated__: Other than providing its own DSL and matcher library, Accord is designed to easily integrate with the larger Scala ecosystem, and provides out-of-the-box support for [Scala.js](http://www.scala-js.org), as well as integration modules for [Spring Validation](spring3.html), [Specs<sup>2</sup>](specs2.html) and [ScalaTest](scalatest.html).

Accord is developed and used at <a href="http://www.wix.com"><img src="images/wix_logo.png" width="42" height="11" alt="Wix.com"></a> and distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0), which basically means you can use and modify it freely. Feedback, bug reports and improvements are welcome!

By Example
==========

<postit>
  :point_right: &nbsp;&nbsp;<em>See the <a href="dsl.html">DSL user guide</a> for the full set of features.</em>
  <br>
  :point_right: &nbsp;&nbsp;<em>See the <a href="api.html">API user guide</a> for details of how descriptions are generated and used.</em>
</postit>


Defining a validator is quite straightforward:

```scala
import com.wix.accord.dsl._    // Import the validator DSL

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

Executing the validator is equally straightforwad:

```scala
// Import the library
import com.wix.accord._

// Validate an object successfully
val validPerson = Person( "Wernher", "von Braun" )
val result: com.wix.accord.Result = validate( validPerson )   // Validator is implicitly resolved
assert( result == Success )

// Or get a detailed failure back:
val invalidPerson = Person( "", "No First Name" )
val failure: com.wix.accord.Result = validate( invalidPerson )
assert( failure == Failure( Set(                          // One or more violations
  RuleViolation(                                          // A violation includes:
    value = "",                                           //   - The invalid value
    constraint = "must not be empty",                     //   - The constraint being violated
    path = Path( Generic( "firstName" ) )                 //   - A path to the violating property
  )
) ) )
```

<a name="getting-started"></a>

Getting Started
===============

<postit>
  :point_right: &nbsp;&nbsp;<em>The <a href="migration-guide.html">migration guide</a> highlights API changes in new versions.</em>
</postit>

Accord version {{ site.version.release }} is available on Maven Central Repository. Scala versions 2.10.3+, 2.11.1+ and 2.12.0+ are supported. The next milestone is {{ site.version.snapshot }} and is available from the Sonatype snapshots repository.

SBT
---

Simply add the `accord-core` module to your build settings:

```scala
// Regular (JVM) Scala projects:
libraryDependencies += "com.wix" %% "accord-core" % "{{ site.version.release }}"

// Scala.js projects:
libraryDependencies += "com.wix" %%% "accord-core" % "{{ site.version.release }}"

// As a result of a Scala compiler bug in versions prior to 2.12.2, in some
// cases you may get "missing or invalid dependency detected while loading 
// class file" errors. Until a fix is released this can be worked around by
// adding the following your build script.
// (See issue #84 at https://github.com/wix/accord/issues/84)
libraryDependencies +=
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided"
```

If you want to evaluate the upcoming snapshot release, add the Sonatype snapshot repository to your resolvers; typically this means adding the following to your `build.sbt` file:

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// Regular (JVM) Scala projects:
libraryDependencies += "com.wix" %% "accord-core" % "{{ site.version.snapshot }}"

// Scala.js projects:
libraryDependencies += "com.wix" %%% "accord-core" % "{{ site.version.snapshot }}"
```

Maven
-----

Accord is published to the Maven Central Repository, so you simply have to add the appropriate dependency to your POM:

```xml
<dependencies>
  <dependency>
    <groupId>com.wix</groupId>
    <artifactId>accord-core_${scala.tools.version}</artifactId>
    <version>{{ site.version.release }}</version>
  </dependency>
  <!--
    As a result of a Scala compiler bug in versions prior to 2.12.1, in some
    cases you may get "missing or invalid dependency detected while loading 
    class file" errors. Until a fix is released this can be worked around by
    adding the following your build script.
    (See issue #84 at https://github.com/wix/accord/issues/84)
  -->
  <dependency>
    <groupId>org.scala-lang</groupId>
    <artifactId>scala-compiler</artifactId>
    <version>${scala.version}</version>
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
    <version>{{ site.version.snapshot }}</version>
  </dependency>
</dependencies>
```

