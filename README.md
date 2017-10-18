# ltest

[![Build Status][travis-badge]][travis]
[![Dependencies Status][deps-badge]][deps]
[![Clojars Project][clojars-badge]][clojars]
[![Tag][tag-badge]][tag]
[![JDK version][jdk-v]](.travis.yml)
[![Clojure version][clojure-v]](project.clj)

[![][logo]][logo-large]

*A custom test runner for clojure.test with detailed, coloured output and summaries*


#### Contents

* [About](#about-)
* [Feature List](#feature-list-)
* [Usage](#usage-)
  * [Function Calls](#function-calls-)
    * [Running Multiple Tests](#running-multiple-tests-)
    * [Running One Test](#running-one-test-)
    * [Running a Suite](#running-a-suite-)
    * [Running Multiple Suites](#running-multiple-suites-)
  * [Creating a Test Runner](#creating-a-test-runner-)
    * [Tagging Test Namespaces](#tagging-test-namespaces-)
    * [A runner Namespace](#a-runner-namespace-)
    * [Adding a lein Alias](#adding-a-lein-alias-)
    * [Running without ltest](#running-without-ltest-)
* [License](#license-)


## About [&#x219F;](#contents)

This project started as a complaint (a numbered list of them) on a Slack
channel about the default test runner for `clojure.test`. Most of these
relating to the fact that there's not quite enough information presented to
maximally assist in debugging ... that one often has to piece things together.
Thanks to [@chris-durbin][chris-durbin], who urged an implementation follow-up
to make things better, ltest for Clojure is now a thing.

The Clojure ltest test runner was inspired by the
[LFE ltest runner][lfe-test-runner], whence it got its name.

The ltest library is currently being used in the NASA Earthdata
[CMR project][cmr] as a supplementary development/testing tool, and in the
[CMR Client][cmr-client] library.


## Feature List [&#x219F;](#contents)

The basic needs ltest aims to resolve (admittedly important for only a subset of
developers) are the following:

* a detailed and explicit reporting-while-testing on what's getting tested
  * &#x2705; namespace
  * &#x2705; function
  * &#x2705; assertion
  * text of `(testing ...)` call (see
    [ticket #11](https://github.com/clojusc/ltest/issues/11))
* &#x2705; explicit test status for each assertion (`OK`, `FAIL`, `ERROR`)
* a separation of reporting-while-testing and failure/error details
  * &#x2705; the running status of tests should be kept visually clean
  * &#x2705; failures and errors should be grouped separately
  * &#x2705; failure and error reporting should be done at the end, after the
    summary, in their own sections
  * suites should hold off until all suites have finished before reporting
    (see [ticket #12](https://github.com/clojusc/ltest/issues/12))
* &#x2705; failure and error reporting should include the full namespace +
  function of where the issue occurred for easier viewing/copying+pasting
* &#x2705; different status output, sections, etc., should use ANSI terminal
  colors to assist with quick and easy identification of issues, data, etc.
* &#x2705; tests should be ordered alphabetically
* &#x2705; users/developers should have the ability to form arbitrary high-level
  divisions of tests
  * &#x2705; useful for running unit tests and integration tests together
  * &#x2705; called "suites" in ltest
  * an opinionated default grouping for suites should be offered
    * &#x2705; by default, group by the top two elements of a namespace (e.g.,
      `a.b.c.d` and `a.b.e.f` would both be grouped in `a.b`)
    * developers should have the ability to override this easily (see
      [ticket #25](https://github.com/clojusc/ltest/issues/25))


## Usage [&#x219F;](#contents)

The functionality provided by this library my be used in several ways:

* As a tool (set of functions) from a development REPL
* As a utility library for creating a test runner for your project
* As the basis for a `lein` plugin (not yet created; see ticket
  [#10](https://github.com/clojusc/ltest/issues/10))

The first two are discussed below. In both cases, the ltest library is
utilized solely through its primary namespace, e.g.:

```clj
(require '[ltest.core :as ltest])
```


### Function Calls [&#x219F;](#contents)


#### Running Multiple Tests [&#x219F;](#contents)

Collections of tests may be run with the `(ltest/run-tests)` function. The
following example passing just one test namespace, but any number may be
passed as additional arguments:

```clj
(ltest/run-tests ['ltest.group1.samples.sample1])
```

Here's is a screenshot of this call's result in the ltest dev environment
(click for a larger view):

[![][screen1-thumb]][screen1]

Note that this includes, in order:
* summary results
* failure listings
* error listings


#### Running One Test [&#x219F;](#contents)

A similar approach with analagous reporting is available for running single
tests, but instead of a namespace, a namespace-qualified test function (as
var) is passed:

```clj
(ltest/run-test #'ltest.group1.samples.sample2/multiple-pass-test)
```

Screenshot:

[![][screen2-thumb]][screen2]


#### Running a Suite [&#x219F;](#contents)

In ltest, test suites are aribitrary named groupings of tests. As with
`run-tests`, any number of namespaces my be provided in the `:nss` vector:

```clj
(ltest/run-suite {:name "Simple Suite"
                  :nss ['ltest.group1.samples.sample2]})
```

Screenshot:

[![][screen3-thumb]][screen3]


#### Running Multiple Suites [&#x219F;](#contents)

You can also define multiple suites and run them together (useful for unit and
integration tests):

```clj
(def suite-1
  {:name "Arbitrary Division 1"
   :nss ['nogroup
         'ltest.group1.samples.sample0
         'ltest.group1.samples.sample1]})

(def suite-2
  {:name "Arbitrary Division 2"
   :nss [:ltest.group1.samples.sample2
         "ltest.group1.samples.sample3"
         'ltest.group2.samples.sample4
         'ltest.group2.samples.sample5
         'ltest.group2.samples.sample6
         'ltest.group2.samples.sample7]})

(def suites
  [suite-1 suite-2])

(ltest/run-suites suites)
```

Screenshot:

[![][screen4-thumb]][screen4]


### Creating a Test Runner [&#x219F;](#contents)

The [CMR client library][cmr-client] has opted to use ltest to build a quick
test runner that can be executed from the command line, via a `lein` alias.
This is really just a workaround until ltest has an official `lein` plugin.

The steps for creating a test runner are given in the following sub-sections.


#### Tagging Test Namespaces [&#x219F;](#contents)

For all namespaces you want to qualify as containing unit tests, simply update
the namespace for the given file, e.g.,

from this:

```clj
(ns ur.proj.tests.util
  ...)
```

to this:

```clj
(ns :unit ur.proj.tests.util
  ...)
```

Likewse for integration and system tests:

```clj
(ns :integration ur.proj.tests.server
  ...)
```

```clj
(ns :system ur.proj.tests.services
  ...)
```


#### A `runner` Namespace [&#x219F;](#contents)

In this particular runner, the "suite" functionality of ltest is not taken
advantage of; instead, our example runner below relies upon metadata tags in
the test namespaces we've made. Note that the `ltest/run-*-tests` functions are
conveniences provided by ltest; if you should want to tag your tests in any
other arbitrary manner, creating convenience functions for your tags (e.g., to
be used by the test runner) is very easy (see the `ltest.core` source for
hints).

Here is a sample runner namespace, intended to be called from the command line:

```clj
(ns ur.proj.testing.runner
  (:require
   [cmr.client.tests]
   [ltest.core :as ltest])
  (:gen-class))

(def tests-regex #"ur\.proj\.tests\..*")

(defn run-tests
  []
  (ltest/run-all-tests tests-regex))

(defn print-header
  []
  (println)
  (println (apply str (repeat 80 "=")))
  (println "Your Project Test Runner")
  (println (apply str (repeat 80 "=")))
  (println))

(defn -main
  "This can be run from `lein` in the following ways:
  * `lein run-tests`
  * `lein run-tests unit`
  * `lein run-tests integration`
  * `lein run-tests system`"
  [& args]
  (print-header)
  (case (keyword (first args))
    :unit (ltest/run-unit-tests tests-regex)
    :integration (ltest/run-integration-tests tests-regex)
    :system (ltest/run-system-tests tests-regex)
    (run-tests)))
```


#### Adding a `lein` Alias [&#x219F;](#contents)

Let's add an alias to easily execute our test runner from the command
line. In your `project.clj` file, add a new section (if you don't already
have it) siblimg to the `:profiles` or `:dependencies` sections.

```clj
  ...
  :aliases {
    ...
    "run-tests"
      ^{:doc "Use the ltest runner for verbose, colourful test output"}
      ["with-profile" "+test" "run" "-m" "ur.proj.testing.runner"]
    ...}
  ...
```

Now you can use that to run the following, optionally limiting tests to what
they have been tagged in their namespace:

* `lein run-tests` (will run all types of tests)
* `lein run-tests unit`
* `lein run-tests integration`
* `lein run-tests system`


#### Running without ltest [&#x219F;](#contents)

In the example above, we used the arbitrary namespace metadata of `:unit`,
`:integration`, and `:system` for our different types of tests. Now that you
have made these annotations in the test namespaces, you can use them directly
with `lein`, too (without using ltest, should you so choose). Simply add this
to your `project.clj` file's `:test` profile:

```clj
  ...
  :profiles {
    ...
    :test {
      ...
      :test-selectors {
        :default :unit
        :unit :unit
        :integration :integration
        :system :system}
      }}}
  ...
```

Now you can run the following to just test the parts of the project you want:

* `lein test` (will just run unit tests)
* `lein test :unit`
* `lein test :integration`
* `lein test :system`


## License [&#x219F;](#contents)

Copyright © 2017, Clojure-Aided Enrichment Center

Distributed under the Apache License, Version 2.0.


<!-- Named page links below: /-->

[logo]: resources/images/test-dummies-small.jpg
[logo-large]: resources/images/test-dummies.jpg

[travis]: https://travis-ci.org/clojusc/ltest
[travis-badge]: https://travis-ci.org/clojusc/ltest.png?branch=master
[deps]: http://jarkeeper.com/clojusc/ltest
[deps-badge]: http://jarkeeper.com/clojusc/ltest/status.svg
[tag-badge]: https://img.shields.io/github/tag/clojusc/ltest.svg
[tag]: https://github.com/clojusc/ltest/tags
[clojure-v]: https://img.shields.io/badge/clojure-1.8.0-blue.svg
[jdk-v]: https://img.shields.io/badge/jdk-1.7+-blue.svg
[clojars]: https://clojars.org/clojusc/ltest
[clojars-badge]: https://img.shields.io/clojars/v/clojusc/ltest.svg

[screen1-thumb]: resources/images/ns-test-thumb.png
[screen1]: resources/images/ns-test.png
[screen2-thumb]: resources/images/single-test-thumb.png
[screen2]: resources/images/single-test.png
[screen3-thumb]: resources/images/suite-test-thumb.png
[screen3]: resources/images/suite-test.png
[screen4-thumb]: resources/images/suites-tests-thumb.png
[screen4]: resources/images/suites-tests.png

[lfe-test-runner]: https://github.com/lfex/ltest#the-lfe-test-runner-
[chris-durbin]: https://github.com/chris-durbin
[cmr]: https://github.com/nasa/Common-Metadata-Repository/
[cmr-client]: https://github.com/oubiwann/cmr-client/
