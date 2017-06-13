# ltest

*A custom test runner for clojure.test with detailed, coloured output and summaries*

The Clojure ltest test runner was inspired by the [LFE ltest runner]().


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
