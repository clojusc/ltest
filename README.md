# ltest

*A custom test runner for clojure.test with detailed, coloured output and summaries*

This project started as a complaint (a numbered list of them) on a Slack
channel about the default test runner for `clojure.test`. Most of these
relating to the fact that there's not quite enough information presented to
maximally assist in debugging ... that one often has to piece things together.
Thanks to @chris-durbin, who urged an implementation follow-up to make things
better, ltest for Clojure is now a thing.


## Feature List

The basic needs ltest aims to resolve (admittedly important for only a subset of
developers) are the following:

* a detailed and explicit reporting-while-testing on what's getting tested
  * namespace
  * function
  * assertion
* explicit test status for each assertion (`OK`, `FAIL`, `ERROR`)
* a separation of reporting-while-testing and failure/error details
  * the running status of tests should be kept visually clean
  * failures and errors should be grouped separately
  * failure and error reporting should be done at the end, after the summary,
    in their own sections
* failure and error reporting should include the full namespace + function of
  where the issue occurred for easier viewing/copying+pasting
* different status output, sections, etc., should use ANSI terminal colors
  to assist with quick and easy identification of issues, data, etc.
* tests should be ordered alphabetically
* an opinionated default grouping should be offered
  * by default, group by the top two elements of a namespace (e.g.,
    `a.b.c.d` and `a.b.e.f` would both be grouped in `a.b`)
  * developers should have the ability ot override this easily
* users/developers should have the ability to form arbitrary high-level
  divisions of tests
  * useful for running unit tests and integration tests together
  * called "suites" in ltest

The Clojure ltest test runner was inspired by the
[LFE ltest runner][lfe-test-runner], whence it got its name.


## Usage

### Running Multiple Tests

Collections of tests may be run with the `(ltest/run-tests)` function. The
following example passing just one test namespace, but any number may be
passed as additional arguments:

```clj
(ltest/run-tests 'ltest.group1.samples.sample2)
```

Here's is a screenshot of this call's result in the ltest dev environment
(click for a larger view):

[![][screen1-thumb]][screen1]

Note that this includes, in order:
* summary results
* failure listings
* error listings


### Running One Test

A similar approach with analagous reporting is available for running single
tests, but instead of a namespace, a namespace-qualified test function (as
var) is passed:

```clj
(ltest/run-test #'ltest.group1.samples.sample2/multiple-pass-test)
```

Screenshot:

[![][screen2-thumb]][screen2]


### Running a Suite

In ltest, test suites are aribitrary named groupings of tests. As with
`run-tests`, any number of namespaces my be provided in the `:nss` vector:

```clj
(ltest/run-suite {:name "Simple Suite"
                  :nss ['ltest.group1.samples.sample2]})
```

Screenshot:

[![][screen3-thumb]][screen3]


### Running Multiple Suites

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


## License

Copyright © 2017, Clojure-Aided Enrichment Center

Distributed under the Apache License, Version 2.0.


<!-- Named page links below: /-->

[screen1-thumb]: resources/images/ns-test-thumb.png
[screen1]: resources/images/ns-test.png
[screen2-thumb]: resources/images/single-test-thumb.png
[screen2]: resources/images/single-test.png
[screen3-thumb]: resources/images/suite-test-thumb.png
[screen3]: resources/images/suite-test.png
[screen4-thumb]: resources/images/suites-tests-thumb.png
[screen4]: resources/images/suites-tests.png

[lfe-test-runner]: https://github.com/lfex/ltest#the-lfe-test-runner-
