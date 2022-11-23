
[![Build Status](https://travis-ci.org/wix/accord.svg?branch=master)](https://travis-ci.org/wix/accord) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/wix/accord?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge) [![Maven Central](https://img.shields.io/maven-central/v/com.wix/accord-core_2.11.svg?maxAge=3600)](http://search.maven.org/#search|gav|1|g:com.wix%20AND%20a:accord*) [![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.17.svg)](https://www.scala-js.org)

---

## NOTICE: This project is no longer maintained. :warning:

This project is no longer maintained. There will be no more new features, fixes and releases. Feel free to fork this repository, use different build system and release this project under different name.

Overview
========

[![Accord](assets/accord-logo-light.png)](http://wix.github.io/accord)

Accord is a validation library written in and for Scala. Compared to [JSR 303](http://jcp.org/en/jsr/detail?id=303) and [Scalaz validation](https://github.com/scalaz/scalaz/blob/scalaz-seven/core/src/main/scala/scalaz/Validation.scala) it aims to provide the following:

* __Composable__: Because JSR 303 is annotation based, validation rules cannot be composed (annotations cannot receive other annotations as parameters). This is a real problem with some Scala features, for example `Option`s or collections. Accord's validation rules are trivially composable.
* __Simple__: Accord provides a dead-simple story for validation rule definition by leveraging macros, as well as the validation call site (see example below).
* __Self-contained__: Accord is macro-based but completely self-contained, and consequently only relies on the Scala runtime and reflection libraries.
* __Integrated__: Other than providing its own DSL and matcher library, Accord is designed to easily integrate with the larger Scala ecosystem, and provides out-of-the-box support for [Scala.js](http://www.scala-js.org), as well as integration modules for [Spring Validation](http://wix.github.io/accord/spring3.html), [Specs<sup>2</sup>](http://wix.github.io/accord/specs2.html) and [ScalaTest](http://wix.github.io/accord/scalatest.html).

For proper user guide and additional documentation please refer to [project website](http://wix.github.io/accord).

Accord is developed and used at <a href="http://www.wix.com"><img src="assets/wix_logo.png" width="42" height="11" alt="Wix.com"></img></a> and distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0), which basically means you can use and modify it freely. Feedback, bug reports and improvements are welcome!
