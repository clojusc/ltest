# ltest

*A custom test runner for clojure.test with detailed, coloured output and summaries*

## Usage

### Running Multiple Tests

Collections of tests may be run with the `(ltest/run-tests)` function:

```clj
(ltest/run-tests 'ltest.group1.samples.sample2)
```

Here's is a screenshot of this call's result in the ltest dev environment
(click for a larger view):

[![][screen1-thumb]][screen1]

### Running One Test

A Similar approach with analagous reporting is available for running single
tests:

```clj
(ltest/run-test #'ltest.group1.samples.sample2/multiple-pass-test)
```

Screenshot:

[![][screen2-thumb]][screen2]

## License

Copyright © 2017, Clojure-Aided Enrichment Center

Distributed under the Apache License, Version 2.0.


<!-- Named page links below: /-->

[screen1-thumb]: resources/images/ns-test-thumb.png
[screen1]: resources/images/ns-test.png
[screen2-thumb]: resources/images/single-test-thumb.png
[screen2]: resources/images/single-test.png
